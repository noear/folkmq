package org.noear.folkmq.client;

import org.noear.folkmq.MqConstants;
import org.noear.socketd.SocketD;
import org.noear.socketd.exception.SocketdConnectionException;
import org.noear.socketd.transport.client.Client;
import org.noear.socketd.transport.client.ClientConfigHandler;
import org.noear.socketd.transport.core.Entity;
import org.noear.socketd.transport.core.Session;
import org.noear.socketd.transport.core.entity.StringEntity;
import org.noear.socketd.transport.core.listener.EventListener;
import org.noear.socketd.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 消息客户端
 *
 * @author noear
 * @since 1.0
 */
public class MqClientImpl extends EventListener implements MqClientInternal {
    private static final Logger log = LoggerFactory.getLogger(MqClientImpl.class);

    //服务端地址
    private String serverUrl;
    //客户端会话
    private Session clientSession;
    //客户端配置
    private ClientConfigHandler clientConfigHandler;
    //订阅字典
    private Map<String, MqSubscription> subscriptionMap = new HashMap<>();

    //自动回执
    private boolean autoAcknowledge = true;

    public MqClientImpl(String serverUrl) {
        this.serverUrl = serverUrl.replace("folkmq://", "sd:tcp://");

        //接收派发指令
        on(MqConstants.MQ_EVENT_DISTRIBUTE, (s, m) -> {
            MqMessageImpl message = null;

            try {
                message = new MqMessageImpl(this, m);
                MqSubscription subscription = subscriptionMap.get(message.getTopic());

                if (subscription != null) {
                    subscription.handle(message);
                }

                //是否自动回执
                if (autoAcknowledge) {
                    acknowledge(message, true);
                }
            } catch (Throwable e) {
                if (message != null) {
                    acknowledge(message, false);
                }
            }
        });
    }

    @Override
    public MqClient connect() throws IOException {
        Client client = SocketD.createClient(this.serverUrl);

        if (clientConfigHandler != null) {
            client.config(clientConfigHandler);
        }

        clientSession = client.listen(this).open();

        return this;
    }

    @Override
    public void disconnect() throws IOException {
        if (clientSession != null) {
            clientSession.close();
        }
    }

    @Override
    public MqClient config(ClientConfigHandler configHandler) {
        clientConfigHandler = configHandler;
        return this;
    }

    /**
     * 自动回执
     */
    @Override
    public MqClient autoAcknowledge(boolean auto) {
        this.autoAcknowledge = auto;
        return this;
    }

    /**
     * 订阅主题
     *
     * @param topic           主题
     * @param consumer        消费者（实例 ip 或 集群 name）
     * @param consumerHandler 消费处理
     */
    @Override
    public void subscribe(String topic, String consumer, MqConsumerHandler consumerHandler) throws IOException {
        MqSubscription subscription = new MqSubscription(topic, consumer, consumerHandler);

        //支持Qos1
        subscriptionMap.put(topic, subscription);

        if (clientSession != null && clientSession.isValid()) {
            Entity entity = new StringEntity("")
                    .meta(MqConstants.MQ_META_TOPIC, subscription.getTopic())
                    .meta(MqConstants.MQ_META_CONSUMER, subscription.getConsumer());

            clientSession.sendAndRequest(MqConstants.MQ_EVENT_SUBSCRIBE, entity);
        }
    }

    /**
     * 发布消息
     *
     * @param topic     主题
     * @param content   消息内容
     * @param scheduled 预定派发时间
     * @param qos       质量等级（0 或 1）
     */
    @Override
    public CompletableFuture<?> publish(String topic, String content, Date scheduled, int qos) throws IOException {
        if (clientSession == null) {
            throw new SocketdConnectionException("Not connected!");
        }

        StringEntity entity = new StringEntity(content);
        entity.meta(MqConstants.MQ_META_TID, Utils.guid());
        entity.meta(MqConstants.MQ_META_TOPIC, topic);
        entity.meta(MqConstants.MQ_META_QOS, String.valueOf(qos));
        if (scheduled == null) {
            entity.meta(MqConstants.MQ_META_SCHEDULED, "0");
        } else {
            entity.meta(MqConstants.MQ_META_SCHEDULED, String.valueOf(scheduled.getTime()));
        }

        CompletableFuture<?> future = new CompletableFuture<>();

        if (qos > 0) {
            //::Qos1
            clientSession.sendAndSubscribe(MqConstants.MQ_EVENT_PUBLISH, entity, r -> {
                future.complete(null);
            });
        } else {
            //::Qos0
            clientSession.send(MqConstants.MQ_EVENT_PUBLISH, entity);
            future.complete(null);
        }

        return future;
    }

    /**
     * 消费回执
     */
    @Override
    public void acknowledge(MqMessageImpl message, boolean isOk) throws IOException {
        //发送“回执”，向服务端反馈消费情况
        if (message.getQos() > 0) {
            clientSession.replyEnd(message.from, new StringEntity("")
                    .meta(MqConstants.MQ_META_ACK, isOk ? "1" : "0"));
        }
    }

    /**
     * 会话打开时
     */
    @Override
    public void onOpen(Session session) throws IOException {
        super.onOpen(session);

        log.info("Client session opened, session={}", session.sessionId());

        //用于重连时重新订阅
        for (MqSubscription subscription : subscriptionMap.values()) {
            Entity entity = new StringEntity("")
                    .meta(MqConstants.MQ_META_TOPIC, subscription.getTopic())
                    .meta(MqConstants.MQ_META_CONSUMER, subscription.getConsumer());

            session.send(MqConstants.MQ_EVENT_SUBSCRIBE, entity);
        }
    }

    /**
     * 会话关闭时
     */
    @Override
    public void onClose(Session session) {
        super.onClose(session);

        log.info("Client session closed, session={}", session.sessionId());
    }

    /**
     * 会话出错时
     */
    @Override
    public void onError(Session session, Throwable error) {
        super.onError(session, error);

        if (log.isWarnEnabled()) {
            log.warn("Client error, session={}", session.sessionId(), error);
        }
    }
}
