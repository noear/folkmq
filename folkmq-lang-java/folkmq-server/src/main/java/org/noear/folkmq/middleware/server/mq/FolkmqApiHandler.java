package org.noear.folkmq.server.pro.mq;

import org.noear.folkmq.common.MqApis;
import org.noear.folkmq.common.MqConstants;
import org.noear.folkmq.common.MqMetasV1;
import org.noear.folkmq.server.MqServiceListener;
import org.noear.folkmq.server.pro.common.MetricsConfig;
import org.noear.folkmq.server.pro.admin.dso.QueueForceService;
import org.noear.folkmq.server.pro.admin.dso.ViewUtils;
import org.noear.folkmq.server.pro.admin.model.QueueVo;
import org.noear.folkmq.server.pro.common.MqServerConfig;
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

    public FolkmqApiHandler(QueueForceService queueForceService, MqServiceListener serviceListener) {
        this.queueForceService = queueForceService;
        this.serviceListener = serviceListener;
    }

    @Override
    public void handle(Session s, Message m) throws IOException {
        String name = m.meta(MqConstants.API_NAME);
        String token = m.meta(MqConstants.API_TOKEN);

        if (StrUtils.isEmpty(MqServerConfig.apiToken)) {
            s.sendAlarm(m, "Api calls are not supported");
            return;
        }

        if (MqServerConfig.apiToken.equals(token) == false) {
            s.sendAlarm(m, "Token is invalid");
            return;
        }


        String topic = m.meta(MqConstants.MQ_META_TOPIC);
        String consumerGroup = m.meta(MqConstants.MQ_META_CONSUMER_GROUP);
        String queueName = topic + MqConstants.SEPARATOR_TOPIC_CONSUMER_GROUP + consumerGroup;


        try {
            if (MqApis.MQ_QUEUE_LIST.equals(name)) {
                //{code,data:[{queue,sessionCount,messageCount,....}]}
                List<QueueVo> queueVolist = ViewUtils.queueView(serviceListener);
                replyDo(s, m, Result.succeed(queueVolist));
                return;
            }

            if (MqApis.MQ_QUEUE_VIEW_MESSAGE.equals(name)) {
                //{code,data:{queue,sessionCount,messageCount,....}}
                QueueVo queueVo = ViewUtils.queueOneView(serviceListener, queueName);
                if (queueVo == null) {
                    replyDo(s, m, Result.failure("Queue does not exist"));
                } else {
                    replyDo(s, m, Result.succeed(queueVo));
                }
                return;
            }

            if (MqApis.MQ_QUEUE_VIEW_SESSION.equals(name)) {
                //{code,data:[ip,ip]}
                List<String> list = ViewUtils.queueSessionListView(serviceListener, queueName);
                replyDo(s, m, Result.succeed(list));
                return;
            }

            if (MqApis.MQ_QUEUE_FORCE_CLEAR.equals(name)) {
                //{code,data}
                queueForceService.forceClear(serviceListener, topic, consumerGroup, MetricsConfig.isStandalone);
                replyDo(s, m, Result.succeed());
                return;
            }

            if (MqApis.MQ_QUEUE_FORCE_DELETE.equals(name)) {
                //{code,data}
                queueForceService.forceDelete(serviceListener, topic, consumerGroup, MetricsConfig.isStandalone);
                replyDo(s, m, Result.succeed());
                return;
            }

            if (MqApis.MQ_QUEUE_FORCE_DISTRIBUTE.equals(name)) {
                //{code,data}
                queueForceService.forceDelete(serviceListener, topic, consumerGroup, MetricsConfig.isStandalone);
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
