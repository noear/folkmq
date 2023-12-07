package org.noear.folkmq.client;

import org.noear.folkmq.common.MqConstants;
import org.noear.socketd.exception.SocketdAlarmException;
import org.noear.socketd.transport.core.Entity;
import org.noear.socketd.transport.core.Session;
import org.noear.socketd.transport.core.entity.StringEntity;
import org.noear.socketd.transport.core.listener.EventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

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
        on(MqConstants.MQ_EVENT_DISTRIBUTE, (s, m) -> {
            MqMessageReceivedImpl message = null;

            try {
                message = new MqMessageReceivedImpl(client, s, m);
                MqSubscription subscription = client.subscriptionMap.get(message.getTopic());

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
                    log.warn("Client consumer handle error, tid={}", message.getTid(), e);
                } else {
                    log.warn("Client consumer handle error", e);
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

        //用于重连时重新订阅
        for (MqSubscription subscription : client.subscriptionMap.values()) {
            Entity entity = new StringEntity("")
                    .meta(MqConstants.MQ_META_TOPIC, subscription.getTopic())
                    .meta(MqConstants.MQ_META_CONSUMER, subscription.getConsumerGroup())
                    .at(MqConstants.BROKER_AT_SERVER);

            session.send(MqConstants.MQ_EVENT_SUBSCRIBE, entity);
        }
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
            if (error instanceof SocketdAlarmException) {
                SocketdAlarmException alarmException = (SocketdAlarmException) error;
                log.warn("Client error, sessionId={}, from={}", session.sessionId(), alarmException.getFrom(), error);
            } else {
                log.warn("Client error, sessionId={}", session.sessionId(), error);
            }
        }
    }
}
