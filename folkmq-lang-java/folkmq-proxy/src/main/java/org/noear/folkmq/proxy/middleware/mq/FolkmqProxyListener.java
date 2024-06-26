package org.noear.folkmq.proxy.middleware.mq;

import org.noear.folkmq.FolkMQ;
import org.noear.folkmq.common.MqConstants;
import org.noear.folkmq.common.MqMetasResolver;
import org.noear.folkmq.common.MqMetasV2;
import org.noear.folkmq.common.MqUtils;
import org.noear.folkmq.broker.MqNextTime;
import org.noear.folkmq.broker.MqQps;
import org.noear.snack.ONode;
import org.noear.socketd.broker.BrokerListener;
import org.noear.socketd.transport.core.*;
import org.noear.socketd.transport.core.entity.EntityDefault;
import org.noear.socketd.transport.core.entity.StringEntity;
import org.noear.socketd.utils.RunUtils;
import org.noear.socketd.utils.SessionUtils;
import org.noear.socketd.utils.StrUtils;
import org.noear.solon.core.Lifecycle;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.ReentrantLock;

/**
 * FolkMq 代理监听
 *
 * @author noear
 * @since 1.0
 */
public class FolkmqProxyListener extends BrokerListener implements Lifecycle {
    private final ProxyApiHandler apiHandler;
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
    private final Map<String, String> accessMap = new HashMap<>();

    //订阅关系表(topic=>topicConsumerGroup[])
    private final Map<String, Set<String>> subscribeMap = new ConcurrentHashMap<>();
    private final ReentrantLock subscribeLock = new ReentrantLock(true);

    public Map<String, Set<String>> getSubscribeMap() {
        return subscribeMap;
    }


    public FolkmqProxyListener(ProxyApiHandler apiHandler) {
        this.apiHandler = apiHandler;

        this.qpsScheduled = RunUtils.delayAndRepeat(() -> {
            qpsInput.reset();
            qpsOutput.reset();
        }, 5_000);
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
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
    public FolkmqProxyListener addAccess(String accessKey, String accessSecretKey) {
        accessMap.put(accessKey, accessSecretKey);
        return this;
    }

    /**
     * 配置访问账号
     *
     * @param accessMap 访问账号集合
     */
    public FolkmqProxyListener addAccessAll(Map<String, String> accessMap) {
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

        if (MqConstants.PROXY_AT_BROKER.equals(session.name())) {
            log.info("Proxy: broker channel opened, sessionId={}, ip={}",
                    session.sessionId(),
                    session.remoteAddress());
        } else {
            //如果不是 server，直接添加为 player
            super.onOpen(session);

            log.info("Proxy: client channel opened, sessionId={}, ip={}",
                    session.sessionId(),
                    session.remoteAddress());
        }
    }

    @Override
    public void onClose(Session session) {
        super.onClose(session);


        //不打印 ip，否则可能异常
        if (MqConstants.PROXY_AT_BROKER.equals(session.name())) {
            log.info("Proxy: broker channel closed, sessionId={}, code={}",
                    session.sessionId(),
                    session.closeCode());
        } else {
            log.info("Proxy: client channel closed, sessionId={}, code={}",
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

    @Override
    public void onMessage(Session requester, Message message) throws IOException {
        //记录流量
        qpsInput.record();
        super.onMessage(requester, message);
    }

    @Override
    public void onReply(Session session, Message message) {
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
            onDistribute(requester, message);
            //结束处理
            return;
        } else if (MqConstants.MQ_EVENT_JOIN.equals(message.event())) {
            //经理加入时，同步订阅
            onJoin(requester, message);
            return;
        } else if (MqConstants.MQ_EVENT_REQUEST.equals(message.event())) {
            onRequest(requester, message);
            return;
        }

        if (MqConstants.MQ_API.equals(message.event())) {
            apiHandler.handle(requester, message);
            return;
        }

        if (message.event().startsWith(MqConstants.ADMIN_PREFIX)) {
            log.warn("Proxy: client channel admin events are not allowed, sessionId={}, ip={}",
                    requester.sessionId(),
                    requester.remoteAddress());
            return;
        }

        super.onMessageDo(requester, message);
    }

    /**
     * 收到经理连接时
     */
    private void onJoin(Session requester, Message message) throws IOException {
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

        log.info("Proxy: broker channel joined, sessionId={}, ip={}",
                requester.sessionId(),
                requester.remoteAddress());

        //答复
        if (message.isRequest()) {
            requester.reply(message, new StringEntity("1"));
        }
    }

    /**
     * 收到 Rpc 请求时（由 client 发起；也可能是 broker 发起的事务确认）
     */
    private void onRequest(Session requester, Message message) throws IOException {
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
    }

    /**
     * 收到派发指令时（由 broker 发起）
     */
    private void onDistribute(Session requester, Message message) throws IOException {
        String atName = message.atName();
        boolean isBroadcast = MqUtils.getLast().isBroadcast(message.entity());

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
    }

    /**
     * 收到订阅指令时（由 client 发起）
     */
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

    /**
     * 订阅执行（实际会转发给 broker）
     */
    public void subscribeDo(Session requester, String topic, String queueName) {
        if (requester != null) {
            requester.attrPut(queueName, "1");
            addPlayer(queueName, requester);
        }

        subscribeLock.lock();

        try {
            //以身份进行订阅(topic=>[topicConsumerGroup])
            Set<String> topicConsumerSet = subscribeMap.computeIfAbsent(topic, n -> Collections.newSetFromMap(new ConcurrentHashMap<>()));
            topicConsumerSet.add(queueName);
        } finally {
            subscribeLock.unlock();
        }
    }

    /**
     * 发布执行（实际会转发给 broker）
     */
    public boolean publishDo(Message routingMessage, int qos) throws IOException {
        Session responder = this.getPlayerAny(MqConstants.PROXY_AT_BROKER, null, null);

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

    /**
     * 执行确认否（实际会转发给 broker）
     */
    private void acknowledgeAsNo(Session requester, Message message) throws IOException {
        //如果没有会话，自动转为ACK失败
        if (message.isSubscribe() || message.isRequest()) {
            MqMetasResolver mr = MqUtils.getOf(message);
            String key = mr.getKey(message);
            String topic = mr.getTopic(message);
            String consumerGroup = mr.getConsumerGroup(message);

            EntityDefault entity = new EntityDefault();
            entity.metaPut(MqMetasV2.MQ_META_VID, FolkMQ.versionCodeAsString());
            entity.metaPut(MqMetasV2.MQ_META_TOPIC, topic);
            entity.metaPut(MqMetasV2.MQ_META_CONSUMER_GROUP, consumerGroup);
            entity.metaPut(MqMetasV2.MQ_META_KEY, key);

            entity.metaPut(MqConstants.MQ_META_ACK, "0");

            requester.replyEnd(message, entity);
        }
    }
}