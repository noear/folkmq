package org.noear.folkmq.client;

import org.noear.folkmq.MqConstants;
import org.noear.socketd.SocketD;
import org.noear.socketd.exception.SocketdConnectionException;
import org.noear.socketd.transport.client.Client;
import org.noear.socketd.transport.client.ClientConfigHandler;
import org.noear.socketd.transport.core.Entity;
import org.noear.socketd.transport.core.Message;
import org.noear.socketd.transport.core.Session;
import org.noear.socketd.transport.core.entity.StringEntity;
import org.noear.socketd.transport.core.listener.BuilderListener;
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
public class MqClientImpl extends BuilderListener implements MqClientInternal {
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
        on(MqConstants.MQ_CMD_DISTRIBUTE, (s, m) -> {
            try {
                String topic = m.meta(MqConstants.MQ_TOPIC);
                MqSubscription subscription = subscriptionMap.get(topic);

                if (subscription != null) {
                    subscription.handle(new MqMessageImpl(this, m));
                }

                //是否自动回执
                if (autoAcknowledge) {
                    acknowledge(m, true);
                }
            } catch (Throwable e) {
                acknowledge(m, false);
            }
        });
    }

    @Override
    public MqClient config(ClientConfigHandler configHandler) {
        clientConfigHandler = configHandler;
        return this;
    }

    @Override
    public MqClient connect() throws IOException {
        Client client = SocketD.createClient(this.serverUrl);

        if (clientConfigHandler != null) {
            client.config(clientConfigHandler);
        }

        clientSession = client.config(c -> c.heartbeatInterval(5_000))
                .listen(this)
                .open();

        return this;
    }

    @Override
    public void disconnect() throws IOException {
        if (clientSession != null) {
            clientSession.close();
        }
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
                    .meta(MqConstants.MQ_TOPIC, subscription.getTopic())
                    .meta(MqConstants.MQ_CONSUMER, subscription.getConsumer());

            clientSession.sendAndRequest(MqConstants.MQ_CMD_SUBSCRIBE, entity);
        }
    }

    /**
     * 发布消息
     *
     * @param topic   主题
     * @param content 消息内容
     */
    @Override
    public CompletableFuture<?> publish(String topic, String content) throws IOException {
        return publish(topic, content, null);
    }

    /**
     * 发布消息
     *
     * @param topic     主题
     * @param content   消息内容
     * @param scheduled 预定派发时间
     */
    @Override
    public CompletableFuture<?> publish(String topic, String content, Date scheduled) throws IOException {
        if(clientSession == null){
            throw new SocketdConnectionException("Not connected!");
        }

        //支持Qos1
        StringEntity entity = new StringEntity(content);
        entity.meta(MqConstants.MQ_TID, Utils.guid());
        entity.meta(MqConstants.MQ_TOPIC, topic);
        if (scheduled == null) {
            entity.meta(MqConstants.MQ_SCHEDULED, "0");
        } else {
            entity.meta(MqConstants.MQ_SCHEDULED, String.valueOf(scheduled.getTime()));
        }

        CompletableFuture<?> future = new CompletableFuture<>();
        clientSession.sendAndSubscribe(MqConstants.MQ_CMD_PUBLISH, entity, r -> {
            future.complete(null);
        });

        return future;
    }


    /**
     * 消费回执
     */
    @Override
    public void acknowledge(Message message, boolean isOk) throws IOException {
        clientSession.replyEnd(message, new StringEntity("")
                .meta(MqConstants.MQ_ACK, isOk ? "1" : "0"));
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
                    .meta(MqConstants.MQ_TOPIC, subscription.getTopic())
                    .meta(MqConstants.MQ_CONSUMER, subscription.getConsumer());

            session.send(MqConstants.MQ_CMD_SUBSCRIBE, entity);
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
