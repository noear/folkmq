package org.noear.folkmq.middleware.broker.mq;

import org.noear.folkmq.FolkMQ;
import org.noear.folkmq.common.MqConstants;
import org.noear.folkmq.common.MqUtils;
import org.noear.folkmq.server.MqNextTime;
import org.noear.folkmq.server.MqQps;
import org.noear.snack.ONode;
import org.noear.socketd.broker.BrokerListenerPlus;
import org.noear.socketd.transport.core.*;
import org.noear.socketd.transport.core.entity.PressureEntity;
import org.noear.socketd.transport.core.entity.StringEntity;
import org.noear.socketd.utils.RunUtils;
import org.noear.socketd.utils.SessionUtils;
import org.noear.socketd.utils.StrUtils;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/**
 * FolkMq 经纪人监听
 *
 * @author noear
 * @since 1.0
 */
public class BrokerListenerFolkmq extends BrokerListenerPlus {
    private final BrokerApiHandler apiHandler;
    private final MqQps qpsInput = new MqQps();
    private final MqQps qpsOutput = new MqQps();
    private final ScheduledFuture<?> qpsScheduled;

    public MqQps getQpsInput() {
        return qpsInput;
    }

    public MqQps getQpsOutput() {
        return qpsOutput;
    }

    //访问账号
    private Map<String, String> accessMap = new HashMap<>();

    //订阅关系表(topic=>topicConsumerGroup[])
    private Map<String, Set<String>> subscribeMap = new ConcurrentHashMap<>();
    private Object SUBSCRIBE_LOCK = new Object();

    public Map<String, Set<String>> getSubscribeMap() {
        return subscribeMap;
    }


    public BrokerListenerFolkmq(BrokerApiHandler apiHandler) {
        this.apiHandler = apiHandler;

        this.qpsScheduled = RunUtils.delayAndRepeat(()->{
            qpsInput.reset();
            qpsOutput.reset();
        },5_000);
    }

    @Override
    public void stop() {
        super.stop();

        if (qpsScheduled != null) {
            qpsScheduled.cancel(true);
        }
    }


    public void removeSubscribe(String topic, String queueName) {
        Set<String> tmp = subscribeMap.get(topic);
        if (tmp != null) {
            tmp.remove(queueName);
        }
    }

    public boolean hasSubscribe(String topic) {
        return subscribeMap.containsKey(topic);
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
        //返馈版本号
        session.handshake().outMeta(MqConstants.FOLKMQ_VERSION, FolkMQ.versionCodeAsString());

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

        if (MqConstants.BROKER_AT_SERVER.equals(session.name())) {
            log.info("Server channel opened, sessionId={}, ip={}",
                    session.sessionId(),
                    session.remoteAddress());
        } else {
            //如果不是 server，直接添加为 player
            super.onOpen(session);

            log.info("Client channel opened, sessionId={}, ip={}",
                    session.sessionId(),
                    session.remoteAddress());
        }
    }

    @Override
    public void onClose(Session session) {
        super.onClose(session);


        //不打印 ip，否则可能异常
        if (MqConstants.BROKER_AT_SERVER.equals(session.name())) {
            log.info("Server channel closed, sessionId={}, code={}",
                    session.sessionId(),
                    session.closeCode());
        } else {
            log.info("Client channel closed, sessionId={}, code={}",
                    session.sessionId(),
                    session.closeCode());
        }


        Collection<String> atList = session.attrMap().keySet();
        if (atList.size() > 0) {
            for (String at : atList) {
                //注销玩家
                removePlayer(at, session);
            }
        }
    }

    private long maxPressure = 1200;
    @Override
    public void onMessage(Session requester, Message message) throws IOException {
        //记录流量
        qpsInput.record();

        if (brokerMessageCounter.longValue() > maxPressure) {
            if (MqConstants.BROKER_AT_SERVER.equals(requester.name()) == false) {
                if (message.meta(EntityMetas.META_X_UNLIMITED) == null) {
                    //如果压力过高
                    requester.sendPressure(message, PressureEntity.getInstance());
                    try {
                        Thread.sleep(10);
                    } catch (Throwable ex) {
                        //乎略
                    }
                }
            }
        }

        super.onMessage(requester, message);
    }

    @Override
    public void onReply(Session session, Reply reply) {
        //记录流量
        qpsInput.record();
    }

    @Override
    public void onSend(Session session, Message message) {
        //记录输出
        qpsOutput.record();
    }

    @Override
    public void onMessageDo(Session requester, Message message) throws IOException {
        if (MqConstants.MQ_EVENT_SUBSCRIBE.equals(message.event())) {
            onSubscribe(requester, message);
        } else if (MqConstants.MQ_EVENT_UNSUBSCRIBE.equals(message.event())) {
            //取消订阅，注销玩家
            String topic = message.meta(MqConstants.MQ_META_TOPIC);
            String consumerGroup = message.meta(MqConstants.MQ_META_CONSUMER_GROUP);
            String queueName = topic + MqConstants.SEPARATOR_TOPIC_CONSUMER_GROUP + consumerGroup;

            removePlayer(queueName, requester);
        } else if (MqConstants.MQ_EVENT_DISTRIBUTE.equals(message.event())) {
            String atName = message.atName();
            boolean isBroadcast = MqUtils.getV2().isBroadcast(message.entity());

            if (isBroadcast) {
                //广播模式
                if (atName.endsWith("!")) {
                    atName = atName.substring(0, atName.length() - 1);
                }

                for (Session s0 : getPlayerAll(atName)) {
                    if (SessionUtils.isActive(s0)) {
                        try {
                            forwardToSession(requester, message, s0, MqNextTime.maxConsumeMillis());
                        } catch (Throwable e) {
                            acknowledgeAsNo(requester, message);
                        }
                    }
                }
            } else {
                //单发模式（给同名的某个玩家，轮询负截均衡）
                Session responder = getPlayerAny(atName, requester, message);
                if (SessionUtils.isActive(responder)) {
                    //转发消息
                    try {
                        forwardToSession(requester, message, responder, MqNextTime.maxConsumeMillis());
                    } catch (Throwable e) {
                        acknowledgeAsNo(requester, message);
                    }
                } else {
                    acknowledgeAsNo(requester, message);
                }
            }

            //结束处理
            return;
        } else if (MqConstants.MQ_EVENT_JOIN.equals(message.event())) {
            //同步订阅
            if (subscribeMap.size() > 0) {
                String json = ONode.stringify(subscribeMap);
                Entity entity = new StringEntity(json)
                        .metaPut(MqConstants.MQ_META_BATCH, "1")
                        .metaPut(EntityMetas.META_X_UNLIMITED, "1");

                //不用 sendAndRequest
                requester.send(MqConstants.MQ_EVENT_SUBSCRIBE, entity);
            }

            //标为不限制
            requester.attrPut(EntityMetas.META_X_UNLIMITED, "1");

            //注册服务
            String name = requester.name();
            if (StrUtils.isNotEmpty(name)) {
                addPlayer(name, requester);
            }

            log.info("Server channel joined, sessionId={}, ip={}",
                    requester.sessionId(),
                    requester.remoteAddress());

            //答复
            if(message.isRequest()) {
                requester.reply(message, new StringEntity("1"));
            }

            //结束处理
            return;
        } else if (MqConstants.MQ_EVENT_REQUEST.equals(message.event())) {
            String atName = message.atName();

            //单发模式（给同名的某个玩家，轮询负截均衡）
            Session responder = getPlayerAny(atName, requester, message);
            if (responder != null && responder.isValid()) {
                //转发消息
                try {
                    forwardToSession(requester, message, responder, MqNextTime.maxConsumeMillis());
                } catch (Throwable e) {
                    requester.sendAlarm(message, "Broker forward '@" + atName + "' error: " + e.getMessage());
                }
            } else {
                requester.sendAlarm(message, "Broker don't have '@" + atName + "' session");
            }
            return;
        }

        if (MqConstants.MQ_API.equals(message.event())) {
            apiHandler.handle(requester, message);
            return;
        }

        if (message.event().startsWith(MqConstants.ADMIN_PREFIX)) {
            log.warn("Client channel admin events are not allowed, sessionId={}, ip={}",
                    requester.sessionId(),
                    requester.remoteAddress());
            return;
        }

        super.onMessageDo(requester, message);
    }

    private void onSubscribe(Session requester, Message message) {
        String is_batch = message.meta(MqConstants.MQ_META_BATCH);
        if ("1".equals(is_batch)) {
            ONode oNode = ONode.loadStr(message.dataAsString());
            Map<String, Collection<String>> subscribeData = oNode.toObject();
            if (subscribeData != null) {
                for (Map.Entry<String, Collection<String>> kv : subscribeData.entrySet()) {
                    for (String queueName : kv.getValue()) {
                        //执行订阅
                        subscribeDo(requester, kv.getKey(), queueName);
                    }
                }
            }
        } else {
            //订阅，注册玩家
            String topic = message.meta(MqConstants.MQ_META_TOPIC);
            String consumerGroup = message.meta(MqConstants.MQ_META_CONSUMER_GROUP);
            String queueName = topic + MqConstants.SEPARATOR_TOPIC_CONSUMER_GROUP + consumerGroup;


            subscribeDo(requester, topic, queueName);
        }
    }

    public void subscribeDo(Session requester, String topic, String queueName) {
        if (requester != null) {
            requester.attrPut(queueName, "1");
            addPlayer(queueName, requester);
        }

        synchronized (SUBSCRIBE_LOCK) {
            //以身份进行订阅(topic=>[topicConsumerGroup])
            Set<String> topicConsumerSet = subscribeMap.computeIfAbsent(topic, n -> Collections.newSetFromMap(new ConcurrentHashMap<>()));
            topicConsumerSet.add(queueName);
        }
    }

    public boolean publishDo(Message routingMessage, int qos) throws IOException {
        Session responder = this.getPlayerAny(MqConstants.BROKER_AT_SERVER, null, null);

        if (responder != null) {
            if (qos > 0) {
                responder.sendAndRequest(MqConstants.MQ_EVENT_PUBLISH, routingMessage).await();
            } else {
                responder.send(MqConstants.MQ_EVENT_PUBLISH, routingMessage);
            }

            return true;
        } else {
            return false;
        }
    }

    private void acknowledgeAsNo(Session requester, Message message) throws IOException {
        //如果没有会话，自动转为ACK失败
        if (message.isSubscribe() || message.isRequest()) {
            requester.replyEnd(message, new StringEntity("")
                    .metaPut(MqConstants.MQ_META_ACK, "0"));
        }
    }
}