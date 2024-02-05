package org.noear.folkmq.server.pro.mq;

import org.noear.folkmq.common.MqApi;
import org.noear.folkmq.common.MqConstants;
import org.noear.folkmq.server.MqServiceListener;
import org.noear.folkmq.server.pro.Config;
import org.noear.folkmq.server.pro.admin.dso.QueueForceService;
import org.noear.folkmq.server.pro.admin.dso.ViewUtils;
import org.noear.folkmq.server.pro.admin.model.QueueVo;
import org.noear.folkmq.server.pro.common.ConfigNames;
import org.noear.snack.ONode;
import org.noear.socketd.transport.core.Message;
import org.noear.socketd.transport.core.Session;
import org.noear.socketd.transport.core.entity.StringEntity;
import org.noear.socketd.transport.core.listener.MessageHandler;
import org.noear.socketd.utils.StrUtils;
import org.noear.solon.Solon;
import org.noear.solon.core.handle.Result;

import java.io.IOException;
import java.util.List;

/**
 * @author noear
 * @since 2.3
 */
public class FolkmqApiHandler implements MessageHandler {
    private MqServiceListener serviceListener;
    private QueueForceService queueForceService;
    private String apiToken;

    public FolkmqApiHandler(QueueForceService queueForceService, MqServiceListener serviceListener) {
        this.queueForceService = queueForceService;
        this.serviceListener = serviceListener;
        this.apiToken = Solon.cfg().get(ConfigNames.folkmq_api_token, "");
    }

    @Override
    public void handle(Session s, Message m) throws IOException {
        String name = m.meta("api.name");
        String token = m.meta("api.token");

        if (StrUtils.isEmpty(apiToken)) {
            s.sendAlarm(m, "Api calls are not supported");
            return;
        }

        if (apiToken.equals(token) == false) {
            s.sendAlarm(m, "Token is invalid");
            return;
        }


        String topic = m.meta(MqConstants.MQ_META_TOPIC);
        String consumerGroup = m.meta(MqConstants.MQ_META_CONSUMER_GROUP);
        String queueName = topic + MqConstants.SEPARATOR_TOPIC_CONSUMER_GROUP + consumerGroup;


        try {
            if (MqApi.MQ_QUEUE_LIST.equals(name)) {
                //{code,data:[{queue,sessionCount,messageCount,....}]}
                List<QueueVo> queueVolist = ViewUtils.queueView(serviceListener);
                replyDo(s, m, Result.succeed(queueVolist));
                return;
            }

            if (MqApi.MQ_QUEUE_VIEW_MESSAGE.equals(name)) {
                //{code,data:{queue,sessionCount,messageCount,....}}
                QueueVo queueVo = ViewUtils.queueOneView(serviceListener, queueName);
                replyDo(s, m, Result.succeed(queueVo));
                return;
            }

            if (MqApi.MQ_QUEUE_VIEW_SESSION.equals(name)) {
                //{code,data:[ip,ip]}
                List<String> list = ViewUtils.queueSessionListView(serviceListener, queueName);
                replyDo(s, m, Result.succeed(list));
                return;
            }

            if (MqApi.MQ_QUEUE_FORCE_CLEAR.equals(name)) {
                //{code,data}
                queueForceService.forceClear(serviceListener, topic, consumerGroup, Config.isStandalone);
                replyDo(s, m, Result.succeed());
                return;
            }

            if (MqApi.MQ_QUEUE_FORCE_DELETE.equals(name)) {
                //{code,data}
                queueForceService.forceDelete(serviceListener, topic, consumerGroup, Config.isStandalone);
                replyDo(s, m, Result.succeed());
                return;
            }

            if (MqApi.MQ_QUEUE_FORCE_DISTRIBUTE.equals(name)) {
                //{code,data}
                queueForceService.forceDelete(serviceListener, topic, consumerGroup, Config.isStandalone);
                replyDo(s, m, Result.succeed());
                return;
            }
        } catch (Throwable e) {
            replyDo(s, m, Result.failure(e.getMessage()));
        }
    }

    private void replyDo(Session s, Message m, Result rst) throws IOException {
        s.replyEnd(m, new StringEntity(ONode.stringify(rst)));
    }
}
