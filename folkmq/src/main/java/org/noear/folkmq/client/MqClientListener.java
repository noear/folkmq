package org.noear.folkmq.client;

import org.noear.folkmq.common.MqConstants;
import org.noear.snack.ONode;
import org.noear.socketd.exception.SocketDAlarmException;
import org.noear.socketd.transport.core.Entity;
import org.noear.socketd.transport.core.Message;
import org.noear.socketd.transport.core.Session;
import org.noear.socketd.transport.core.entity.StringEntity;
import org.noear.socketd.transport.core.listener.EventListener;
import org.noear.socketd.utils.RunUtils;
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
            try {
                MqMessageReceivedImpl message = new MqMessageReceivedImpl(client, s, m);

                if (message.isSequence()) {
                    RunUtils.single(() -> onDistribute(s, m, message));
                } else {
                    if (client.handleExecutor == null) {
                        RunUtils.async(() -> onDistribute(s, m, message));
                    } else {
                        client.handleExecutor.submit(() -> onDistribute(s, m, message));
                    }
                }
            } catch (Throwable e) {
                log.warn("Client consume handle error, sid={}", m.sid(), e);
            }
        });

        doOn(MqConstants.MQ_EVENT_REQUEST, (s, m) -> {
            try {
                MqMessageReceivedImpl message = new MqMessageReceivedImpl(client, s, m);

                if (client.handleExecutor == null) {
                    RunUtils.async(() -> onDistribute(s, m, message));
                } else {
                    client.handleExecutor.submit(() -> onDistribute(s, m, message));
                }
            } catch (Throwable e) {
                log.warn("Client consume handle error, sid={}", m.sid(), e);
            }
        });
    }

    private void onDistribute(Session s, Message m, MqMessageReceivedImpl message) {
        if (message.isTransaction()) {
            try {
                client.transactionListenser.consume(message);
            } catch (Throwable e) {
                try {
                    s.sendAlarm(m, "Request handle error:" + e.getMessage());
                    log.warn("Client request handle error, tid={}", message.getTid(), e);
                } catch (Throwable err) {
                    log.warn("Client request handle error, tid={}", message.getTid(), e);
                }
            }
        } else {
            MqSubscription subscription = client.getSubscription(message.getTopic(), message.getConsumerGroup());

            try {
                if (subscription != null) {
                    //有订阅
                    subscription.consume(message);

                    //是否自动回执
                    if (subscription.isAutoAck()) {
                        client.acknowledge(s, m, message, true, null);
                    }
                } else {
                    //没有订阅
                    client.acknowledge(s, m, message, false, null);
                }
            } catch (Throwable e) {
                try {
                    if (subscription != null) {
                        //有订阅
                        if (subscription.isAutoAck()) {
                            client.acknowledge(s, m, message, false, null);
                        }
                    } else {
                        //没有订阅
                        client.acknowledge(s, m, message, false, null);
                    }

                    log.warn("Client consume handle error, tid={}", message.getTid(), e);
                } catch (Throwable err) {
                    log.warn("Client consume handle error, tid={}", message.getTid(), e);
                }
            }
        }
    }

    /**
     * 会话打开时
     */
    @Override
    public void onOpen(Session session) throws IOException {
        super.onOpen(session);

        log.info("Client session opened, sessionId={}", session.sessionId());

        if (client.getSubscriptionSize() == 0) {
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

        session.sendAndRequest(MqConstants.MQ_EVENT_SUBSCRIBE, entity, 30_000).await();

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