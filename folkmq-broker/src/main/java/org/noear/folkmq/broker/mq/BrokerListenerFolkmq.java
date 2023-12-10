package org.noear.folkmq.broker.mq;

import org.noear.folkmq.common.MqConstants;
import org.noear.socketd.broker.BrokerListener;
import org.noear.socketd.transport.core.Message;
import org.noear.socketd.transport.core.Session;
import org.noear.socketd.transport.core.entity.StringEntity;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * FolkMq 经纪人监听
 *
 * @author noear
 * @since 1.0
 */
public class BrokerListenerFolkmq extends BrokerListener {
    //访问账号
    private Map<String, String> accessMap = new HashMap<>();

    //订阅关系表(topic=>topicConsumer[])
    private Map<String, Set<String>> subscribeMap = new ConcurrentHashMap<>();
    private Object SUBSCRIBE_LOCK = new Object();

    public Map<String, Set<String>> getSubscribeMap() {
        return subscribeMap;
    }

    /**
     * 配置访问账号
     *
     * @param accessKey       访问者身份
     * @param accessSecretKey 访问者密钥
     */
    public BrokerListenerFolkmq addAccess(String accessKey, String accessSecretKey) {
        accessMap.put(accessKey, accessSecretKey);
        return this;
    }

    /**
     * 配置访问账号
     *
     * @param accessMap 访问账号集合
     */
    public BrokerListenerFolkmq addAccessAll(Map<String, String> accessMap) {
        if (accessMap != null) {
            this.accessMap.putAll(accessMap);
        }
        return this;
    }

    @Override
    public void onOpen(Session session) throws IOException {
        if (accessMap.size() > 0) {
            //如果有 ak/sk 配置，则进行鉴权
            String accessKey = session.param(MqConstants.PARAM_ACCESS_KEY);
            String accessSecretKey = session.param(MqConstants.PARAM_ACCESS_SECRET_KEY);

            if (accessKey == null || accessSecretKey == null) {
                session.close();
                return;
            }

            if (accessSecretKey.equals(accessMap.get(accessKey)) == false) {
                session.close();
                return;
            }
        }

        super.onOpen(session);

        log.info("Server channel opened, sessionId={}", session.sessionId());
    }

    @Override
    public void onClose(Session session) {
        super.onClose(session);

        Collection<String> atList = session.attrMap().keySet();
        if (atList.size() > 0) {
            for (String at : atList) {
                //注销玩家
                removePlayer(at, session);
            }
        }
    }

    @Override
    public void onMessage(Session requester, Message message) throws IOException {
        if (MqConstants.MQ_EVENT_SUBSCRIBE.equals(message.event())) {
            //订阅，注册玩家
            String topic = message.meta(MqConstants.MQ_META_TOPIC);
            String consumerGroup = message.meta(MqConstants.MQ_META_CONSUMER_GROUP);
            String queueName = topic + MqConstants.SEPARATOR_TOPIC_CONSUMER_GROUP + consumerGroup;

            requester.attr(queueName, "1");
            addPlayer(queueName, requester);

            subscribeDo(topic, queueName);
        } else if (MqConstants.MQ_EVENT_UNSUBSCRIBE.equals(message.event())) {
            //取消订阅，注销玩家
            String topic = message.meta(MqConstants.MQ_META_TOPIC);
            String consumerGroup = message.meta(MqConstants.MQ_META_CONSUMER_GROUP);
            String queueName = topic + MqConstants.SEPARATOR_TOPIC_CONSUMER_GROUP + consumerGroup;

            removePlayer(queueName, requester);
        } else if (MqConstants.MQ_EVENT_DISTRIBUTE.equals(message.event())) {
            String atName = message.at();

            //单发模式（给同名的某个玩家，轮询负截均衡）
            Session responder = getPlayerOne(atName);
            if (responder != null && responder.isValid()) {
                //转发消息
                try {
                    forwardToSession(requester, message, responder);
                } catch (Throwable e) {
                    ackNo(requester, message);
                }
            } else {
                ackNo(requester, message);
            }
            return;
        }

        super.onMessage(requester, message);
    }

    private void ackNo(Session requester, Message message) throws IOException{
        //如果没有会话，自动转为ACK失败
        if (message.isSubscribe() || message.isRequest()) {
            requester.replyEnd(message, new StringEntity("")
                    .meta(MqConstants.MQ_META_ACK, "0"));
        }
    }

    public void subscribeDo(String topic, String queueName) {

        synchronized (SUBSCRIBE_LOCK) {
            //以身份进行订阅(topic=>[topicConsumer])
            Set<String> topicConsumerSet = subscribeMap.computeIfAbsent(topic, n -> Collections.newSetFromMap(new ConcurrentHashMap<>()));
            topicConsumerSet.add(queueName);
        }
    }
}
