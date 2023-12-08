package org.noear.folkmq.client;

import org.noear.folkmq.common.MqConstants;
import org.noear.folkmq.exception.FolkmqException;
import org.noear.socketd.SocketD;
import org.noear.socketd.cluster.ClusterClientSession;
import org.noear.socketd.exception.SocketdConnectionException;
import org.noear.socketd.exception.SocketdException;
import org.noear.socketd.transport.client.ClientConfigHandler;
import org.noear.socketd.transport.client.ClientSession;
import org.noear.socketd.transport.core.Entity;
import org.noear.socketd.transport.core.Message;
import org.noear.socketd.transport.core.Session;
import org.noear.socketd.transport.core.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 消息客户端默认实现
 *
 * @author noear
 * @since 1.0
 */
public class MqClientDefault implements MqClientInternal {
    private static final Logger log = LoggerFactory.getLogger(MqClientDefault.class);

    //服务端地址
    private final List<String> serverUrls;
    //客户端会话
    private ClusterClientSession clientSession;
    //客户端监听
    private final MqClientListener clientListener;
    //客户端配置
    private ClientConfigHandler clientConfigHandler;
    //订阅字典
    protected Map<String, MqSubscription> subscriptionMap = new HashMap<>();

    //自动回执
    protected boolean autoAcknowledge = true;
    //发布重试
    protected int publishRetryTimes = 2;

    public MqClientDefault(String... urls) {
        this.serverUrls = new ArrayList<>();
        this.clientListener = new MqClientListener(this);

        for (String url : urls) {
            url = url.replaceAll("folkmq://", "sd:tcp://");
            serverUrls.add(url);
        }
    }

    @Override
    public MqClient connect() throws IOException {
        clientSession = (ClusterClientSession) SocketD.createClusterClient(serverUrls)
                .config(clientConfigHandler)
                .listen(clientListener)
                .open();

        return this;
    }

    @Override
    public void disconnect() throws IOException {
        clientSession.close();
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

    @Override
    public MqClient publishRetryTimes(int times) {
        this.publishRetryTimes = times;
        return this;
    }

    /**
     * 订阅主题
     *
     * @param topic           主题
     * @param consumerGroup   消费者组
     * @param consumerHandler 消费处理
     */
    @Override
    public void subscribe(String topic, String consumerGroup, MqConsumeHandler consumerHandler) throws IOException {
        MqSubscription subscription = new MqSubscription(topic, consumerGroup, consumerHandler);

        //支持Qos1
        subscriptionMap.put(topic, subscription);

        if (clientSession != null) {
            for (ClientSession session : clientSession.getSessionAll()) {
                //如果有连接会话，则执行订阅
                Entity entity = new StringEntity("")
                        .meta(MqConstants.MQ_META_TOPIC, subscription.getTopic())
                        .meta(MqConstants.MQ_META_CONSUMER_GROUP, subscription.getConsumerGroup())
                        .at(MqConstants.BROKER_AT_SERVER_ALL);

                session.sendAndRequest(MqConstants.MQ_EVENT_SUBSCRIBE, entity);

                log.info("Client subscribe successfully: {}#{}, sessionId={}", topic, consumerGroup, session.sessionId());
            }
        }
    }

    @Override
    public void unsubscribe(String topic, String consumerGroup) throws IOException {
        subscriptionMap.remove(topic);

        if (clientSession != null) {
            for (ClientSession session : clientSession.getSessionAll()) {
                //如果有连接会话
                Entity entity = new StringEntity("")
                        .meta(MqConstants.MQ_META_TOPIC, topic)
                        .meta(MqConstants.MQ_META_CONSUMER_GROUP, consumerGroup)
                        .at(MqConstants.BROKER_AT_SERVER_ALL);

                session.sendAndRequest(MqConstants.MQ_EVENT_UNSUBSCRIBE, entity);

                log.info("Client unsubscribe successfully: {}#{}， sessionId={}", topic, consumerGroup, session.sessionId());
            }
        }
    }

    @Override
    public void publish(String topic, IMqMessage message) throws IOException {
        if (clientSession == null) {
            throw new SocketdConnectionException("Not connected!");
        }

        ClientSession session = clientSession.getSessionOne();
        if (session == null || session.isValid() == false) {
            throw new SocketdException("No session is available!");
        }

        Entity entity = publishEntityBuildDo(topic, message);

        publishRetryDo(session, message, entity, null);
    }

    /**
     * 发布消息
     *
     * @param topic   主题
     * @param message 消息
     */
    @Override
    public CompletableFuture<Boolean> publishAsync(String topic, IMqMessage message) throws IOException {
        if (clientSession == null) {
            throw new SocketdConnectionException("Not connected!");
        }

        ClientSession session = clientSession.getSessionOne();
        if (session == null || session.isValid() == false) {
            throw new SocketdException("No session is available!");
        }

        CompletableFuture<Boolean> future = new CompletableFuture<>();
        Entity entity = publishEntityBuildDo(topic, message);

        publishRetryDo(session, message, entity, future);

        return future;
    }

    /**
     * 发布重试执行
     */
    private void publishRetryDo(ClientSession session, IMqMessage message, Entity entity, CompletableFuture<Boolean> future) throws IOException {
        if (publishRetryTimes > 1) {
            //多次重试
            for (int i = publishRetryTimes; i > 0; i--) {
                try {
                    publishDo(session, message, entity, future);
                    break;
                } catch (Throwable e) {
                    if (i == 1) {
                        throw e;
                    }
                }
            }
        } else {
            //单次
            publishDo(session, message, entity, future);
        }
    }

    /**
     * 发布执行
     */
    private void publishDo(ClientSession session, IMqMessage message, Entity entity, CompletableFuture<Boolean> future) throws IOException {
        if (message.getQos() > 0) {
            //::Qos1
            if (future != null) {
                //异步
                session.sendAndRequest(MqConstants.MQ_EVENT_PUBLISH, entity, r -> {
                    int confirm = Integer.parseInt(r.metaOrDefault(MqConstants.MQ_META_CONFIRM, "0"));
                    if (confirm == 1) {
                        future.complete(true);
                    } else {
                        String messsage = "Client message publish confirm failed: " + r.dataAsString();
                        future.completeExceptionally(new FolkmqException(messsage));
                    }
                });
            } else {
                //同步
                Entity resp = session.sendAndRequest(MqConstants.MQ_EVENT_PUBLISH, entity);

                int confirm = Integer.parseInt(resp.metaOrDefault(MqConstants.MQ_META_CONFIRM, "0"));
                if (confirm != 1) {
                    String messsage = "Client message publish confirm failed: " + resp.dataAsString();
                    throw new FolkmqException(messsage);
                }
            }
        } else {
            //::Qos0
            session.send(MqConstants.MQ_EVENT_PUBLISH, entity);

            if (future != null) {
                future.complete(true);
            }
        }
    }

    /**
     * 发布实体构建
     */
    private Entity publishEntityBuildDo(String topic, IMqMessage message) {
        //构建消息实体
        StringEntity entity = new StringEntity(message.getContent());
        entity.meta(MqConstants.MQ_META_TID, message.getTid());
        entity.meta(MqConstants.MQ_META_TOPIC, topic);
        entity.meta(MqConstants.MQ_META_QOS, (message.getQos() == 0 ? "0" : "1"));
        if (message.getScheduled() == null) {
            entity.meta(MqConstants.MQ_META_SCHEDULED, "0");
        } else {
            entity.meta(MqConstants.MQ_META_SCHEDULED, String.valueOf(message.getScheduled().getTime()));
        }
        entity.at(MqConstants.BROKER_AT_SERVER);

        return entity;
    }

    /**
     * 消费回执
     *
     * @param message 收到的消息
     * @param isOk    回执
     */
    @Override
    public void acknowledge(Session session, Message from, MqMessageReceivedImpl message, boolean isOk) throws IOException {
        //发送“回执”，向服务端反馈消费情况
        if (message.getQos() > 0) {
            if (session.isValid()) {
                session.replyEnd(from, new StringEntity("")
                        .meta(MqConstants.MQ_META_ACK, isOk ? "1" : "0"));
            }
        }
    }

    /**
     * 关闭
     */
    @Override
    public void close() throws IOException {
        clientSession.close();
    }
}