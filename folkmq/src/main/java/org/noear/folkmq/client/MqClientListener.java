package org.noear.folkmq.client;

import org.noear.folkmq.common.MqConstants;
import org.noear.snack.ONode;
import org.noear.socketd.exception.SocketDAlarmException;
import org.noear.socketd.transport.core.Entity;
import org.noear.socketd.transport.core.Session;
import org.noear.socketd.transport.core.entity.StringEntity;
import org.noear.socketd.transport.core.listener.EventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 * 客户端监听器
 *
 * @author noear
 * @since 1.0
 */
public class MqClientListener extends EventListener {
    private static final Logger log = LoggerFactory.getLogger(MqClientListener.class);
    private final MqClientDefault client;

    public MqClientListener(MqClientDefault client) {
        this.client = client;

        //接收派发指令
        doOn(MqConstants.MQ_EVENT_DISTRIBUTE, (s, m) -> {
            MqMessageReceivedImpl message = null;

            try {
                message = new MqMessageReceivedImpl(client, s, m);
                MqSubscription subscription = client.getSubscription(message.getTopic(), message.getConsumerGroup());

                if (subscription != null) {
                    subscription.consume(message);
                }

                //是否自动回执
                if (client.autoAcknowledge) {
                    client.acknowledge(s, m, message, true);
                }
            } catch (Throwable e) {
                if (message != null) {
                    client.acknowledge(s, m, message, false);
                    log.warn("Client consume handle error, tid={}", message.getTid(), e);
                } else {
                    log.warn("Client consume handle error", e);
                }
            }
        });
    }

    /**
     * 会话打开时
     */
    @Override
    public void onOpen(Session session) throws IOException {
        super.onOpen(session);

        log.info("Client session opened, sessionId={}", session.sessionId());

        if(client.getSubscriptionSize() == 0){
            return;
        }

        //用于重连时重新订阅
        Map<String, Set<String>> subscribeData = new HashMap<>();
        for (MqSubscription subscription : client.getSubscriptionAll()) {
            Set<String> queueNameSet = subscribeData.computeIfAbsent(subscription.getTopic(), n -> new HashSet<>());
            queueNameSet.add(subscription.getQueueName());
        }

        String json = ONode.stringify(subscribeData);
        Entity entity = new StringEntity(json)
                .metaPut(MqConstants.MQ_META_BATCH, "1")
                .at(MqConstants.BROKER_AT_SERVER);

        session.sendAndRequest(MqConstants.MQ_EVENT_SUBSCRIBE, entity,30_000).await();

        log.info("Client onOpen batch subscribe successfully, sessionId={}", session.sessionId());
    }

    /**
     * 会话关闭时
     */
    @Override
    public void onClose(Session session) {
        super.onClose(session);

        log.info("Client session closed, sessionId={}", session.sessionId());
    }

    /**
     * 会话出错时
     */
    @Override
    public void onError(Session session, Throwable error) {
        super.onError(session, error);

        if (log.isWarnEnabled()) {
            if (error instanceof SocketDAlarmException) {
                SocketDAlarmException alarmException = (SocketDAlarmException) error;
                log.warn("Client error, sessionId={}, from={}", session.sessionId(), alarmException.getAlarm(), error);
            } else {
                log.warn("Client error, sessionId={}", session.sessionId(), error);
            }
        }
    }
}
