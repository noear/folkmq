package org.noear.folkmq.client;

import org.noear.folkmq.FolkMQ;
import org.noear.folkmq.common.MqAssert;
import org.noear.folkmq.common.MqConstants;
import org.noear.folkmq.common.MqMetasV2;
import org.noear.folkmq.common.MqUtils;
import org.noear.folkmq.exception.FolkmqException;
import org.noear.folkmq.common.MqTopicHelper;
import org.noear.socketd.SocketD;
import org.noear.socketd.cluster.ClusterClientSession;
import org.noear.socketd.exception.SocketDConnectionException;
import org.noear.socketd.exception.SocketDException;
import org.noear.socketd.transport.client.ClientConfigHandler;
import org.noear.socketd.transport.client.ClientSession;
import org.noear.socketd.transport.core.Entity;
import org.noear.socketd.transport.core.EntityMetas;
import org.noear.socketd.transport.core.Message;
import org.noear.socketd.transport.core.Session;
import org.noear.socketd.transport.core.entity.EntityDefault;
import org.noear.socketd.transport.core.entity.StringEntity;
import org.noear.socketd.transport.stream.RequestStream;
import org.noear.socketd.utils.StrUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * 消息客户端默认实现
 *
 * @author noear
 * @since 1.0
 * @since 1.2
 */
public class MqClientDefault implements MqClientInternal {
    private static final Logger log = LoggerFactory.getLogger(MqClientDefault.class);

    //事务回查
    protected MqTransactionCheckback transactionCheckback;
    //监听处理
    protected MqConsumeHandler listenHandler;
    //消费执行器
    protected ExecutorService consumeExecutor;
    //服务端地址
    private final String[] urls;
    //客户端会话
    private ClusterClientSession clientSession;
    //客户端监听
    private final MqClientListener clientListener;
    //客户端配置
    private ClientConfigHandler clientConfigHandler;
    //订阅字典
    private Map<String, MqSubscription> subscriptionMap = new HashMap<>();
    //客户端名字
    private String name;
    //命名空间
    private String namespace;


    //自动回执
    protected boolean autoAcknowledge = true;

    public MqClientDefault(String[] urls) {
        this(urls, null);
    }

    public MqClientDefault(String[] urls, MqClientListener clientListener) {
        this.urls = urls;

        if (clientListener == null) {
            this.clientListener = new MqClientListener();
        } else {
            this.clientListener = clientListener;
        }

        this.clientListener.init(this);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public MqClient nameAs(String name) {
        MqAssert.requireNonNull(name, "Param 'name' can't be null");
        MqAssert.assertMeta(name, "name");
        MqAssert.assertMetaSymbols(name, "name", '/', "/");

        this.name = name;
        return this;
    }

    @Override
    public String namespace() {
        return namespace;
    }

    @Override
    public MqClient namespaceAs(String namespace) {
        this.namespace = namespace;

        if (StrUtils.isNotEmpty(namespace)) {
            MqAssert.assertMeta(namespace, "namespace");
            MqAssert.assertMetaSymbols(namespace, "namespace", '/', "/");
        }

        return this;
    }

    @Override
    public MqClient connect() throws IOException {
        List<String> serverUrls = new ArrayList<>();

        for (String url : urls) {
            url = url.replaceAll("folkmq:ws://", "sd:ws://");
            url = url.replaceAll("folkmq:wss://", "sd:wss://");
            url = url.replaceAll("folkmq://", "sd:tcp://");

            for (String url1 : url.split(",")) {
                if (StrUtils.isNotEmpty(name)) {
                    if (url1.contains("?")) {
                        url1 = url1 + "&@=" + name;
                    } else {
                        url1 = url1 + "?@=" + name;
                    }
                }

                serverUrls.add(url1);
            }
        }


        //默认不缩小分片，方便无锁发送
        clientSession = (ClusterClientSession) SocketD.createClusterClient(serverUrls)
                .config(c -> {
                    c.metaPut(MqConstants.FOLKMQ_VERSION, FolkMQ.versionCodeAsString())
                            .heartbeatInterval(6_000)
                            .maxMemoryRatio(0.8F)
                            .nolockSend(true)
                            .ioThreads(1)
                            .codecThreads(1)
                            .exchangeThreads(1);

                    if (StrUtils.isNotEmpty(namespace)) {
                        c.metaPut(MqConstants.FOLKMQ_NAMESPACE, namespace);
                    }

                    if (clientConfigHandler != null) {
                        clientConfigHandler.clientConfig(c);
                    }
                })
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

    @Override
    public MqClient consumeExecutor(ExecutorService consumeExecutor) {
        this.consumeExecutor = consumeExecutor;
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
    public boolean autoAcknowledge() {
        return autoAcknowledge;
    }

    @Override
    public CompletableFuture<String> call(String apiName, String apiToken, String topic, String consumerGroup) throws IOException {
        MqAssert.requireNonNull(apiName, "Param 'apiName' can't be null");
        MqAssert.requireNonNull(apiToken, "Param 'apiToken' can't be null");
        MqAssert.requireNonNull(topic, "Param 'topic' can't be null");
        MqAssert.requireNonNull(consumerGroup, "Param 'consumerGroup' can't be null");

        MqAssert.assertMeta(apiName, "apiName");
        MqAssert.assertMeta(apiToken, "apiToken");
        MqAssert.assertMeta(topic, "topic");
        MqAssert.assertMeta(consumerGroup, "consumerGroup");

        //支持命名空间
        topic = MqTopicHelper.getFullTopic(namespace, topic);

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
    public void subscribe(String topic, String consumerGroup, boolean autoAck, MqConsumeHandler consumerHandler) throws IOException {
        MqAssert.requireNonNull(topic, "Param 'topic' can't be null");
        MqAssert.requireNonNull(consumerGroup, "Param 'consumerGroup' can't be null");
        MqAssert.requireNonNull(consumerHandler, "Param 'consumerHandler' can't be null");

        MqAssert.assertMeta(topic, "topic");
        MqAssert.assertMeta(consumerGroup, "consumerGroup");

        //支持命名空间
        topic = MqTopicHelper.getFullTopic(namespace, topic);

        MqSubscription subscription = new MqSubscription(topic, consumerGroup, autoAck, consumerHandler);

        subscriptionMap.put(subscription.getQueueName(), subscription);

        if (clientSession != null) {
            for (ClientSession session : clientSession.getSessionAll()) {
                //如果有连接会话，则执行订阅
                Entity entity = new StringEntity("")
                        .metaPut(MqConstants.MQ_META_TOPIC, subscription.getTopic())
                        .metaPut(MqConstants.MQ_META_CONSUMER_GROUP, subscription.getConsumerGroup())
                        .metaPut(EntityMetas.META_X_UNLIMITED, "1")
                        .at(MqConstants.BROKER_AT_SERVER_ALL);

                //使用 Qos1
                session.sendAndRequest(MqConstants.MQ_EVENT_SUBSCRIBE, entity, 30_000).await();

                log.info("Client subscribe successfully: {}#{}, sessionId={}", topic, consumerGroup, session.sessionId());
            }
        }
    }

    @Override
    public void unsubscribe(String topic, String consumerGroup) throws IOException {
        MqAssert.requireNonNull(topic, "Param 'topic' can't be null");
        MqAssert.requireNonNull(consumerGroup, "Param 'consumerGroup' can't be null");

        MqAssert.assertMeta(topic, "topic");
        MqAssert.assertMeta(consumerGroup, "consumerGroup");

        //支持命名空间
        topic = MqTopicHelper.getFullTopic(namespace, topic);

        String queueName = topic + MqConstants.SEPARATOR_TOPIC_CONSUMER_GROUP + consumerGroup;
        subscriptionMap.remove(queueName);

        if (clientSession != null) {
            for (ClientSession session : clientSession.getSessionAll()) {
                //如果有连接会话
                Entity entity = new StringEntity("")
                        .metaPut(MqConstants.MQ_META_TOPIC, topic)
                        .metaPut(MqConstants.MQ_META_CONSUMER_GROUP, consumerGroup)
                        .metaPut(EntityMetas.META_X_UNLIMITED, "1")
                        .at(MqConstants.BROKER_AT_SERVER_ALL);

                //使用 Qos1
                session.sendAndRequest(MqConstants.MQ_EVENT_UNSUBSCRIBE, entity, 30_000).await();

                log.info("Client unsubscribe successfully: {}#{}， sessionId={}", topic, consumerGroup, session.sessionId());
            }
        }
    }

    @Override
    public void publish(String topic, MqMessage message) throws IOException {
        MqAssert.requireNonNull(topic, "Param 'topic' can't be null");
        MqAssert.requireNonNull(message, "Param 'message' can't be null");

        MqAssert.assertMeta(topic, "topic");

        if (clientSession == null) {
            throw new SocketDConnectionException("Not connected!");
        }

        //支持命名空间
        topic = MqTopicHelper.getFullTopic(namespace, topic);

        ClientSession session = clientSession.getSessionAny(diversionOrNull(topic, message));
        if (session == null || session.isValid() == false) {
            throw new SocketDException("No session is available!");
        }

        Entity entity = MqUtils.getOf((Session) session).publishEntityBuild(topic, message);

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
        MqAssert.requireNonNull(topic, "Param 'topic' can't be null");
        MqAssert.requireNonNull(message, "Param 'message' can't be null");

        MqAssert.assertMeta(topic, "topic");

        if (clientSession == null) {
            throw new SocketDConnectionException("Not connected!");
        }

        //支持命名空间
        topic = MqTopicHelper.getFullTopic(namespace, topic);

        ClientSession session = clientSession.getSessionAny(diversionOrNull(topic, message));
        if (session == null || session.isValid() == false) {
            throw new SocketDException("No session is available!");
        }

        CompletableFuture<Boolean> future = new CompletableFuture<>();
        Entity entity = MqUtils.getOf((Session) session).publishEntityBuild(topic, message);

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
            }).thenError(err -> {
                String messsage = "Client message publish confirm failed: " + err.getMessage();
                future.completeExceptionally(new FolkmqException(messsage));
            });
        } else {
            //::Qos0
            session.send(MqConstants.MQ_EVENT_PUBLISH, entity);
            future.complete(true);
        }

        return future;
    }

    @Override
    public void unpublish(String topic, String key) throws IOException {
        MqAssert.requireNonNull(topic, "Param 'topic' can't be null");
        MqAssert.requireNonNull(key, "Param 'key' can't be null");

        MqAssert.assertMeta(topic, "topic");
        MqAssert.assertMeta(key, "key");

        if (clientSession == null) {
            throw new SocketDConnectionException("Not connected!");
        }

        //支持命名空间
        topic = MqTopicHelper.getFullTopic(namespace, topic);

        ClientSession session = clientSession.getSessionAny(null);
        if (session == null || session.isValid() == false) {
            throw new SocketDException("No session is available!");
        }

        Entity entity = new StringEntity("")
                .metaPut(MqConstants.MQ_META_TOPIC, topic)
                .metaPut(MqConstants.MQ_META_KEY, key)
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
    public CompletableFuture<Boolean> unpublishAsync(String topic, String key) throws IOException {
        MqAssert.requireNonNull(topic, "Param 'topic' can't be null");
        MqAssert.requireNonNull(key, "Param 'key' can't be null");

        MqAssert.assertMeta(topic, "topic");
        MqAssert.assertMeta(key, "key");

        if (clientSession == null) {
            throw new SocketDConnectionException("Not connected!");
        }

        //支持命名空间
        topic = MqTopicHelper.getFullTopic(namespace, topic);

        ClientSession session = clientSession.getSessionAny(null);
        if (session == null || session.isValid() == false) {
            throw new SocketDException("No session is available!");
        }

        CompletableFuture<Boolean> future = new CompletableFuture<>();
        Entity entity = new StringEntity("")
                .metaPut(MqConstants.MQ_META_TOPIC, topic)
                .metaPut(MqConstants.MQ_META_KEY, key)
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
        }).thenError(err -> {
            String messsage = "Client message unpublish confirm failed: " + err.getMessage();
            future.completeExceptionally(new FolkmqException(messsage));
        });

        return future;
    }

    @Override
    public void listen(MqConsumeHandler listenHandler) {
        //检查必要条件
        if (StrUtils.isEmpty(name)) {
            throw new IllegalArgumentException("Client 'name' can't be empty");
        }

        this.listenHandler = listenHandler;
    }

    @Override
    public RequestStream send(MqMessage message, String toName, long timeout) throws IOException {
        //检查必要条件
        if (StrUtils.isEmpty(name)) {
            throw new IllegalArgumentException("Client 'name' can't be empty");
        }

        //检查参数
        MqAssert.requireNonNull(toName, "Param 'toName' can't be null");
        MqAssert.requireNonNull(message, "Param 'message' can't be null");

        MqAssert.assertMeta(toName, "toName");

        if (clientSession == null) {
            throw new SocketDConnectionException("Not connected!");
        }

        ClientSession session = clientSession.getSessionAny(null);
        if (session == null || session.isValid() == false) {
            throw new SocketDException("No session is available!");
        }

        message.internalSender(name());
        EntityDefault entity = MqUtils.getOf((Session) session).publishEntityBuild("", message);
        entity.putMeta(MqMetasV2.MQ_META_CONSUMER_GROUP, toName);
        entity.at(toName);

        if (message.getQos() > 0) {
            //::Qos1
            return session.sendAndRequest(MqConstants.MQ_EVENT_REQUEST, entity, timeout);
        } else {
            //::Qos0
            session.send(MqConstants.MQ_EVENT_REQUEST, entity);
            return null;
        }
    }

    /**
     * 事务回查
     */
    @Override
    public MqClient transactionCheckback(MqTransactionCheckback transactionCheckback) {
        if (transactionCheckback != null) {
            this.transactionCheckback = transactionCheckback;
        }

        return this;
    }

    /**
     * 创建事务
     */
    @Override
    public MqTransaction newTransaction() {
        //检查必要条件
        if (StrUtils.isEmpty(name)) {
            throw new IllegalArgumentException("Client 'name' can't be empty");
        }

        return new MqTransactionImpl(this);
    }

    /**
     * 发布二次提交
     *
     * @param tmid       事务管理id
     * @param keyAry     消息主键集合
     * @param isRollback 是否回滚
     */
    @Override
    public void publish2(String tmid, List<String> keyAry, boolean isRollback) throws IOException {
        if (keyAry == null || keyAry.size() == 0) {
            return;
        }

        if (clientSession == null) {
            throw new SocketDConnectionException("Not connected!");
        }

        ClientSession session = clientSession.getSessionAny(tmid);
        if (session == null || session.isValid() == false) {
            throw new SocketDException("No session is available!");
        }

        Entity entity = new StringEntity(String.join(",", keyAry))
                .metaPut(MqConstants.MQ_META_ROLLBACK, (isRollback ? "1" : "0"))
                .at(MqConstants.BROKER_AT_SERVER_HASH); //事务走哈希

        //::Qos1
        Entity resp = session.sendAndRequest(MqConstants.MQ_EVENT_PUBLISH2, entity).await();

        int confirm = Integer.parseInt(resp.metaOrDefault(MqConstants.MQ_META_CONFIRM, "0"));
        if (confirm != 1) {
            String messsage = "Client message publish2 confirm failed: " + resp.dataAsString();
            throw new FolkmqException(messsage);
        }
    }

    /**
     * 消费答复
     *
     * @param session 会话
     * @param from    来源消息
     * @param message 收到的消息
     * @param isOk    回执
     * @param entity  实体
     */
    @Override
    public void reply(Session session, Message from, MqMessageReceivedImpl message, boolean isOk, Entity entity) throws IOException {
        //发送“回执”，向服务端反馈消费情况
        if (message.getQos() > 0) {
            if (session.isValid()) {
                if (entity == null) {
                    entity = new EntityDefault();
                }

                if (entity instanceof MqAlarm) {
                    session.sendAlarm(from, entity.dataAsString());
                } else {
                    entity.putMeta(MqConstants.MQ_META_ACK, isOk ? "1" : "0");
                    session.replyEnd(from, entity);
                }
            }
        }
    }

    protected String diversionOrNull(String fullTopic, MqMessage message) {
        if (message.isTransaction()) {
            return message.getTmid();
        } else if (message.isSequence()) {
            if (StrUtils.isEmpty(message.getSequenceSharding())) {
                return fullTopic;
            } else {
                return message.getSequenceSharding();
            }
        } else {
            return null;
        }
    }

    protected MqSubscription getSubscription(String fullTopic, String consumerGroup) {
        String queueName = fullTopic + MqConstants.SEPARATOR_TOPIC_CONSUMER_GROUP + consumerGroup;
        return subscriptionMap.get(queueName);
    }

    protected Collection<MqSubscription> getSubscriptionAll() {
        return subscriptionMap.values();
    }

    protected int getSubscriptionSize() {
        return subscriptionMap.size();
    }
}