package org.noear.folkmq.broker.mq;

import org.noear.folkmq.broker.admin.dso.QueueForceService;
import org.noear.folkmq.broker.admin.dso.ViewQueueService;
import org.noear.folkmq.broker.admin.model.QueueVo;
import org.noear.folkmq.broker.common.MqBrokerConfig;
import org.noear.folkmq.common.MqApis;
import org.noear.folkmq.common.MqConstants;
import org.noear.snack.ONode;
import org.noear.socketd.transport.core.Message;
import org.noear.socketd.transport.core.Session;
import org.noear.socketd.transport.core.entity.StringEntity;
import org.noear.socketd.transport.core.listener.MessageHandler;
import org.noear.socketd.utils.StrUtils;
import org.noear.solon.core.handle.Result;

import java.io.IOException;
import java.util.List;

/**
 * @author noear
 * @since 2.3
 */
public class BrokerApiHandler implements MessageHandler {
    private final ViewQueueService queueService;
    private final QueueForceService queueForceService;

    public BrokerApiHandler(QueueForceService queueForceService) {
        this.queueForceService = queueForceService;
        this.queueService = queueForceService.getViewQueueService();
    }

    @Override
    public void handle(Session s, Message m) throws IOException {
        String name = m.meta(MqConstants.API_NAME);
        String token = m.meta(MqConstants.API_TOKEN);

        if (StrUtils.isEmpty(MqBrokerConfig.apiToken)) {
            s.sendAlarm(m, "Api calls are not supported");
            return;
        }

        if (MqBrokerConfig.apiToken.equals(token) == false) {
            s.sendAlarm(m, "Token is invalid");
            return;
        }


        String topic = m.meta(MqConstants.MQ_META_TOPIC);
        String consumerGroup = m.meta(MqConstants.MQ_META_CONSUMER_GROUP);
        String queueName = topic + MqConstants.SEPARATOR_TOPIC_CONSUMER_GROUP + consumerGroup;


        try {
            if (MqApis.MQ_QUEUE_LIST.equals(name)) {
                //{code,data:[{queue,sessionCount,messageCount,....}]}
                List<QueueVo> queueVolist = queueService.getQueueListVo();
                replyDo(s, m, Result.succeed(queueVolist));
                return;
            }

            if (MqApis.MQ_QUEUE_VIEW_MESSAGE.equals(name)) {
                //{code,data:{queue,sessionCount,messageCount,....}}
                QueueVo queueVo = queueService.getQueueVo(queueName);
                if (queueVo == null) {
                    replyDo(s, m, Result.failure("Queue does not exist"));
                } else {
                    replyDo(s, m, Result.succeed(queueVo));
                }
                return;
            }

            if (MqApis.MQ_QUEUE_VIEW_SESSION.equals(name)) {
                //{code,data:[ip,ip]}
                List<String> list = queueService.getQueueSessionList(queueName);
                replyDo(s, m, Result.succeed(list));
                return;
            }

            if (MqApis.MQ_QUEUE_FORCE_CLEAR.equals(name)) {
                //{code,data}
                queueForceService.forceClear(topic, consumerGroup);
                replyDo(s, m, Result.succeed());
                return;
            }

            if (MqApis.MQ_QUEUE_FORCE_DELETE.equals(name)) {
                //{code,data}
                queueForceService.forceDelete(topic, consumerGroup);
                replyDo(s, m, Result.succeed());
                return;
            }

            if (MqApis.MQ_QUEUE_FORCE_DISTRIBUTE.equals(name)) {
                //{code,data}
                queueForceService.forceDelete(topic, consumerGroup);
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
