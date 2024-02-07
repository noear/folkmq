package org.noear.folkmq.client;

import org.noear.folkmq.common.MqConstants;
import org.noear.folkmq.common.MqUtils;
import org.noear.folkmq.exception.FolkmqException;
import org.noear.socketd.SocketD;
import org.noear.socketd.cluster.ClusterClientSession;
import org.noear.socketd.exception.SocketDConnectionException;
import org.noear.socketd.exception.SocketDException;
import org.noear.socketd.transport.client.ClientConfigHandler;
import org.noear.socketd.transport.client.ClientSession;
import org.noear.socketd.transport.core.Entity;
import org.noear.socketd.transport.core.Message;
import org.noear.socketd.transport.core.Session;
import org.noear.socketd.transport.core.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
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
    private Map<String, MqSubscription> subscriptionMap = new HashMap<>();

    //自动回执
    protected boolean autoAcknowledge = true;

    public MqClientDefault(String... urls) {
        this.serverUrls = new ArrayList<>();
        this.clientListener = new MqClientListener(this);

        for (String url : urls) {
            url = url.replaceAll("folkmq:ws://", "sd:ws://");
            url = url.replaceAll("folkmq://", "sd:tcp://");
            serverUrls.add(url);
        }
    }

    @Override
    public MqClient connect() throws IOException {
        clientSession = (ClusterClientSession) SocketD.createClusterClient(serverUrls)
                .config(c -> c.sequenceMode(true).fragmentSize(MqConstants.MAX_FRAGMENT_SIZE))
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
    public CompletableFuture<String> call(String apiName, String apiToken, String topic, String consumerGroup) throws IOException {
        Objects.requireNonNull(apiName, "Param 'apiName' can not be null");
        Objects.requireNonNull(apiToken, "Param 'apiToken' can not be null");
        Objects.requireNonNull(topic, "Param 'topic' can not be null");
        Objects.requireNonNull(consumerGroup, "Param 'consumerGroup' can not be null");

        if (clientSession != null) {
            Entity entity = new StringEntity("")
                    .metaPut(MqConstants.API_NAME, apiName)
                    .metaPut(MqConstants.API_TOKEN, apiToken)
                    .metaPut(MqConstants.MQ_META_TOPIC, topic)
                    .metaPut(MqConstants.MQ_META_CONSUMER_GROUP, consumerGroup);

            CompletableFuture<String> completableFuture = new CompletableFuture<>();
            clientSession.sendAndRequest(MqConstants.MQ_API, entity).thenReply(r -> {
                completableFuture.complete(r.dataAsString());
            }).thenError(err -> {
                completableFuture.completeExceptionally(err);
            });

            return completableFuture;
        } else {
            throw new IOException("No sessions are available");
        }
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
        Objects.requireNonNull(topic, "Param 'topic' can not be null");
        Objects.requireNonNull(consumerGroup, "Param 'consumerGroup' can not be null");
        Objects.requireNonNull(consumerHandler, "Param 'consumerHandler' can not be null");

        MqSubscription subscription = new MqSubscription(topic, consumerGroup, consumerHandler);

        subscriptionMap.put(subscription.getQueueName(), subscription);

        if (clientSession != null) {
            for (ClientSession session : clientSession.getSessionAll()) {
                //如果有连接会话，则执行订阅
                Entity entity = new StringEntity("")
                        .metaPut(MqConstants.MQ_META_TOPIC, subscription.getTopic())
                        .metaPut(MqConstants.MQ_META_CONSUMER_GROUP, subscription.getConsumerGroup())
                        .at(MqConstants.BROKER_AT_SERVER_ALL);

                //使用 Qos1
                session.sendAndRequest(MqConstants.MQ_EVENT_SUBSCRIBE, entity, 30_000).await();

                log.info("Client subscribe successfully: {}#{}, sessionId={}", topic, consumerGroup, session.sessionId());
            }
        }
    }

    @Override
    public void unsubscribe(String topic, String consumerGroup) throws IOException {
        Objects.requireNonNull(topic, "Param 'topic' can not be null");
        Objects.requireNonNull(consumerGroup, "Param 'consumerGroup' can not be null");

        String queueName = topic + MqConstants.SEPARATOR_TOPIC_CONSUMER_GROUP + consumerGroup;
        subscriptionMap.remove(queueName);

        if (clientSession != null) {
            for (ClientSession session : clientSession.getSessionAll()) {
                //如果有连接会话
                Entity entity = new StringEntity("")
                        .metaPut(MqConstants.MQ_META_TOPIC, topic)
                        .metaPut(MqConstants.MQ_META_CONSUMER_GROUP, consumerGroup)
                        .at(MqConstants.BROKER_AT_SERVER_ALL);

                //使用 Qos1
                session.sendAndRequest(MqConstants.MQ_EVENT_UNSUBSCRIBE, entity, 30_000).await();

                log.info("Client unsubscribe successfully: {}#{}， sessionId={}", topic, consumerGroup, session.sessionId());
            }
        }
    }

    @Override
    public void publish(String topic, MqMessage message) throws IOException {
        Objects.requireNonNull(topic, "Param 'topic' can not be null");
        Objects.requireNonNull(message, "Param 'message' can not be null");

        if (clientSession == null) {
            throw new SocketDConnectionException("Not connected!");
        }

        ClientSession session = clientSession.getSessionAny(message.getSequence() ? topic : null);
        if (session == null || session.isValid() == false) {
            throw new SocketDException("No session is available!");
        }

        Entity entity = MqUtils.publishEntityBuild(topic, message);

        if (message.getQos() > 0) {
            //::Qos1
            Entity resp = session.sendAndRequest(MqConstants.MQ_EVENT_PUBLISH, entity).await();

            int confirm = Integer.parseInt(resp.metaOrDefault(MqConstants.MQ_META_CONFIRM, "0"));
            if (confirm != 1) {
                String messsage = "Client message publish confirm failed: " + resp.dataAsString();
                throw new FolkmqException(messsage);
            }
        } else {
            //::Qos0
            session.send(MqConstants.MQ_EVENT_PUBLISH, entity);
        }
    }

    /**
     * 发布消息
     *
     * @param topic   主题
     * @param message 消息
     */
    @Override
    public CompletableFuture<Boolean> publishAsync(String topic, MqMessage message) throws IOException {
        Objects.requireNonNull(topic, "Param 'topic' can not be null");
        Objects.requireNonNull(message, "Param 'message' can not be null");


        if (clientSession == null) {
            throw new SocketDConnectionException("Not connected!");
        }

        ClientSession session = clientSession.getSessionAny(message.getSequence() ? topic : null);
        if (session == null || session.isValid() == false) {
            throw new SocketDException("No session is available!");
        }

        CompletableFuture<Boolean> future = new CompletableFuture<>();
        Entity entity = MqUtils.publishEntityBuild(topic, message);

        if (message.getQos() > 0) {
            //::Qos1
            session.sendAndRequest(MqConstants.MQ_EVENT_PUBLISH, entity, -1).thenReply(r -> {
                int confirm = Integer.parseInt(r.metaOrDefault(MqConstants.MQ_META_CONFIRM, "0"));
                if (confirm == 1) {
                    future.complete(true);
                } else {
                    String messsage = "Client message publish confirm failed: " + r.dataAsString();
                    future.completeExceptionally(new FolkmqException(messsage));
                }
            });
        } else {
            //::Qos0
            session.send(MqConstants.MQ_EVENT_PUBLISH, entity);
            future.complete(true);
        }

        return future;
    }

    @Override
    public void unpublish(String topic, String tid) throws IOException {
        Objects.requireNonNull(topic, "Param 'topic' can not be null");
        Objects.requireNonNull(tid, "Param 'tid' can not be null");


        if (clientSession == null) {
            throw new SocketDConnectionException("Not connected!");
        }

        ClientSession session = clientSession.getSessionAny(null);
        if (session == null || session.isValid() == false) {
            throw new SocketDException("No session is available!");
        }

        Entity entity = new StringEntity("")
                .metaPut(MqConstants.MQ_META_TOPIC, topic)
                .metaPut(MqConstants.MQ_META_TID, tid)
                .at(MqConstants.BROKER_AT_SERVER_ALL);

        //::Qos1
        Entity resp = session.sendAndRequest(MqConstants.MQ_EVENT_UNPUBLISH, entity).await();

        int confirm = Integer.parseInt(resp.metaOrDefault(MqConstants.MQ_META_CONFIRM, "0"));
        if (confirm != 1) {
            String messsage = "Client message unpublish confirm failed: " + resp.dataAsString();
            throw new FolkmqException(messsage);
        }
    }

    @Override
    public CompletableFuture<Boolean> unpublishAsync(String topic, String tid) throws IOException {
        Objects.requireNonNull(topic, "Param 'topic' can not be null");
        Objects.requireNonNull(tid, "Param 'tid' can not be null");


        if (clientSession == null) {
            throw new SocketDConnectionException("Not connected!");
        }

        ClientSession session = clientSession.getSessionAny(null);
        if (session == null || session.isValid() == false) {
            throw new SocketDException("No session is available!");
        }

        CompletableFuture<Boolean> future = new CompletableFuture<>();
        Entity entity = new StringEntity("")
                .metaPut(MqConstants.MQ_META_TOPIC, topic)
                .metaPut(MqConstants.MQ_META_TID, tid)
                .at(MqConstants.BROKER_AT_SERVER_ALL);

        //::Qos1
        session.sendAndRequest(MqConstants.MQ_EVENT_UNPUBLISH, entity, -1).thenReply(r -> {
            int confirm = Integer.parseInt(r.metaOrDefault(MqConstants.MQ_META_CONFIRM, "0"));
            if (confirm == 1) {
                future.complete(true);
            } else {
                String messsage = "Client message unpublish confirm failed: " + r.dataAsString();
                future.completeExceptionally(new FolkmqException(messsage));
            }
        });

        return future;
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
                        .metaPut(MqConstants.MQ_META_ACK, isOk ? "1" : "0"));
            }
        }
    }

    protected MqSubscription getSubscription(String topic, String consumerGroup) {
        String queueName = topic + MqConstants.SEPARATOR_TOPIC_CONSUMER_GROUP + consumerGroup;
        return subscriptionMap.get(queueName);
    }

    protected Collection<MqSubscription> getSubscriptionAll() {
        return subscriptionMap.values();
    }

    protected int getSubscriptionSize() {
        return subscriptionMap.size();
    }

    /**
     * 关闭
     */
    @Override
    public void close() throws IOException {
        clientSession.close();
    }
}