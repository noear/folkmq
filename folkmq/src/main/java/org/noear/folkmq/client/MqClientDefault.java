package org.noear.folkmq.client;

import org.noear.folkmq.common.MqConstants;
import org.noear.folkmq.exception.FolkmqException;
import org.noear.socketd.SocketD;
import org.noear.socketd.exception.SocketdConnectionException;
import org.noear.socketd.transport.client.Client;
import org.noear.socketd.transport.client.ClientConfigHandler;
import org.noear.socketd.transport.core.Entity;
import org.noear.socketd.transport.core.Message;
import org.noear.socketd.transport.core.Session;
import org.noear.socketd.transport.core.entity.StringEntity;
import org.noear.socketd.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 消息客户端默认实现
 *
 * @author noear
 * @since 1.0
 */
public class MqClientDefault implements MqClientInternal {
    private static final Logger log = LoggerFactory.getLogger(MqClientDefault.class);

    //服务端地址
    private String serverUrl;
    //客户端会话
    private List<Session> clientSessions;
    private AtomicInteger clientRoundCounter = new AtomicInteger(0);
    private MqClientListener clientListener;
    //客户端配置
    private ClientConfigHandler clientConfigHandler;
    //订阅字典
    protected Map<String, MqSubscription> subscriptionMap = new HashMap<>();

    //自动回执
    protected boolean autoAcknowledge = true;
    //发布重试
    protected int publishRetryTimes = 2;

    public MqClientDefault(String serverUrl) {
        this.serverUrl = serverUrl.replaceAll("folkmq://", "sd:tcp://");
        this.clientSessions = new ArrayList<>();
        this.clientListener = new MqClientListener(this);
    }

    private Session getSessionOne() {
        if (clientSessions.size() == 0) {
            //没有会话
            return null;
        } else if (clientSessions.size() == 1) {
            return clientSessions.get(0);
        } else {
            List<Session> sessions = clientSessions.stream()
                    .filter(s -> s.isValid())
                    .collect(Collectors.toList());

            if (sessions.size() == 0) {
                //没有会话可用
                return null;
            }

            if (sessions.size() == 1) {
                return sessions.get(0);
            }

            //论询处理
            int counter = clientRoundCounter.incrementAndGet();
            int idx = counter % sessions.size();
            if (counter > 999_999_999) {
                clientRoundCounter.set(0);
            }
            return sessions.get(idx);
        }
    }

    @Override
    public MqClient connect() throws IOException {
        for (String server : serverUrl.split(",")) {
            server = server.trim();

            if (Utils.isNotEmpty(server)) {
                Client client = SocketD.createClient(server);

                if (clientConfigHandler != null) {
                    client.config(clientConfigHandler);
                }

                Session session = client.listen(clientListener).open();
                clientSessions.add(session);
            }
        }

        return this;
    }

    @Override
    public void disconnect() throws IOException {
        for (Session session : clientSessions) {
            session.close();
        }

        clientSessions.clear();
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
     * @param consumer        消费者（实例 ip 或 集群 name）
     * @param consumerHandler 消费处理
     */
    @Override
    public void subscribe(String topic, String consumer, MqConsumeHandler consumerHandler) throws IOException {
        MqSubscription subscription = new MqSubscription(topic, consumer, consumerHandler);

        //支持Qos1
        subscriptionMap.put(topic, subscription);

        for (Session session : clientSessions) {
            //如果有连接会话
            Entity entity = new StringEntity("")
                    .meta(MqConstants.MQ_META_TOPIC, subscription.getTopic())
                    .meta(MqConstants.MQ_META_CONSUMER, subscription.getConsumer())
                    .at(MqConstants.BROKER_AT_SERVER_ALL);

            session.sendAndRequest(MqConstants.MQ_EVENT_SUBSCRIBE, entity);

            log.info("Client subscribe successfully: {}#{}, sessionId={}", topic, consumer, session.sessionId());
        }
    }

    @Override
    public void unsubscribe(String topic, String consumer) throws IOException {
        subscriptionMap.remove(topic);

        for (Session session : clientSessions) {
            //如果有连接会话
            Entity entity = new StringEntity("")
                    .meta(MqConstants.MQ_META_TOPIC, topic)
                    .meta(MqConstants.MQ_META_CONSUMER, consumer)
                    .at(MqConstants.BROKER_AT_SERVER_ALL);

            session.sendAndRequest(MqConstants.MQ_EVENT_UNSUBSCRIBE, entity);

            log.info("Client unsubscribe successfully: {}#{}， sessionId={}", topic, consumer, session.sessionId());
        }
    }

    @Override
    public void publish(String topic, IMqMessage message) throws IOException {
        if (clientSessions.size() == 0) {
            throw new SocketdConnectionException("Not connected!");
        }

        Session session = getSessionOne();
        if (session == null || session.isValid() == false) {
            throw new SocketdConnectionException("No connection is available!");
        }

        Entity entity = publishEntityBuild(topic, message);

        if (message.getQos() > 0) {
            //::Qos1
            Entity resp = null;
            if (publishRetryTimes > 0) {
                //多次重试
                int times = publishRetryTimes;
                while (times > 0) {
                    try {
                        resp = session.sendAndRequest(MqConstants.MQ_EVENT_PUBLISH, entity);
                        break;
                    } catch (Throwable e) {
                        times--;
                        if (times == 0) {
                            throw e;
                        }
                    }
                }
            } else {
                //单次
                resp = session.sendAndRequest(MqConstants.MQ_EVENT_PUBLISH, entity);
            }

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
    public CompletableFuture<Boolean> publishAsync(String topic, IMqMessage message) throws IOException {
        if (clientSessions.size() == 0) {
            throw new SocketdConnectionException("Not connected!");
        }

        Session session = getSessionOne();
        if (session == null || session.isValid() == false) {
            throw new SocketdConnectionException("No connection is available!");
        }

        CompletableFuture<Boolean> future = new CompletableFuture<>();
        Entity entity = publishEntityBuild(topic, message);

        if (message.getQos() > 0) {
            //采用异常 + 可选等待
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
            //::Qos0
            session.send(MqConstants.MQ_EVENT_PUBLISH, entity);
            future.complete(true);
        }

        return future;
    }

    private Entity publishEntityBuild(String topic, IMqMessage message) {
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
            //此处用 replyEnd 不安全，时间长久可能会话断连过（流就无效了）
            session.replyEnd(from, new StringEntity("")
                    .meta(MqConstants.MQ_META_ACK, isOk ? "1" : "0"));
        }
    }
}