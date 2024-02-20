package org.noear.folkmq.server;

import org.noear.folkmq.common.MqConstants;
import org.noear.socketd.transport.core.Message;
import org.noear.socketd.transport.core.Session;
import org.noear.socketd.transport.core.listener.EventListener;
import org.noear.socketd.utils.StrUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author noear
 * @since 1.1
 */
public abstract class MqServiceListenerBase extends EventListener implements MqServiceInternal {
    protected static final Logger log = LoggerFactory.getLogger(MqServiceListener.class);


    //观察者
    protected MqWatcher watcher;
    //群集模式（有经理人的模式）
    protected boolean brokerMode;
    //订阅锁
    protected Object SUBSCRIBE_LOCK = new Object();
    //所有会话
    protected Map<String, Session> sessionAllMap = new ConcurrentHashMap<>();
    //服务端访问账号
    protected Map<String, String> serverAccessMap = new ConcurrentHashMap<>();

    //订阅关系(topic=>[queueName]) //queueName='topic#consumer'
    protected Map<String, Set<String>> subscribeMap = new ConcurrentHashMap<>();
    //队列字典(queueName=>Queue)
    protected Map<String, MqQueue> queueMap = new ConcurrentHashMap<>();
    //预备消息
    protected Map<String, Message> readyMessageMap = new ConcurrentHashMap<>();

    //派发线程
    protected Thread distributeThread;

    /**
     * 获取所有会话
     */
    @Override
    public Collection<Session> getSessionAll() {
        return sessionAllMap.values();
    }

    /**
     * 获取订阅集合
     */
    @Override
    public Map<String, Set<String>> getSubscribeMap() {
        return Collections.unmodifiableMap(subscribeMap);
    }

    /**
     * 获取队列集合
     */
    @Override
    public Map<String, MqQueue> getQueueMap() {
        return Collections.unmodifiableMap(queueMap);
    }

    /**
     * 移除队列
     */
    @Override
    public void removeQueue(String queueName) {
        //先删订阅关系
        String[] ss = queueName.split(MqConstants.SEPARATOR_TOPIC_CONSUMER_GROUP);
        Set<String> tmp = subscribeMap.get(ss[0]);
        tmp.remove(queueName);

        //再删队列
        queueMap.remove(queueName);
    }

    /**
     * 执行订阅
     */
    @Override
    public void subscribeDo(String topic, String consumerGroup, Session session) {
        String queueName = topic + MqConstants.SEPARATOR_TOPIC_CONSUMER_GROUP + consumerGroup;

        synchronized (SUBSCRIBE_LOCK) {
            //::1.构建订阅关系

            //建立订阅关系(topic=>[queueName]) //queueName='topic#consumer'
            Set<String> queueNameSet = subscribeMap.computeIfAbsent(topic, n -> Collections.newSetFromMap(new ConcurrentHashMap<>()));
            queueNameSet.add(queueName);

            //队列映射关系(queueName=>Queue)
            MqQueue queue = queueMap.get(queueName);
            if (queue == null) {
                queue = new MqQueueDefault(watcher, topic, consumerGroup, queueName);
                queueMap.put(queueName, queue);
            }

            //::2.标识会话身份（从持久层恢复时，会话可能为 null）

            if (session != null) {
                log.info("Server channel subscribe topic={}, consumerGroup={}, sessionId={}", topic, consumerGroup, session.sessionId());

                //会话绑定队列（可以绑定多个队列）
                session.attrPut(queueName, "1");

                //加入队列会话
                queue.addSession(session);
            }
        }
    }

    /**
     * 执行取消订阅
     */
    @Override
    public void unsubscribeDo(String topic, String consumerGroup, Session session) {
        if (session == null) {
            return;
        }

        log.info("Server channel unsubscribe topic={}, consumerGroup={}, sessionId={}", topic, consumerGroup, session.sessionId());

        String queueName = topic + MqConstants.SEPARATOR_TOPIC_CONSUMER_GROUP + consumerGroup;

        //1.获取相关队列
        MqQueue queue = queueMap.get(queueName);

        //2.移除队列绑定
        session.attrMap().remove(queueName);

        //3.退出队列会话
        if (queue != null) {
            queue.removeSession(session);
        }
    }

    /**
     * 执行路由
     */
    @Override
    public void routingDo(Message message) {
        String tid = message.meta(MqConstants.MQ_META_TID);
        //可能是非法消息
        if (StrUtils.isEmpty(tid)) {
            log.warn("The tid cannot be null, sid={}", message.sid());
            return;
        }

        //复用解析
        String topic = message.meta(MqConstants.MQ_META_TOPIC);
        int qos = "0".equals(message.meta(MqConstants.MQ_META_QOS)) ? 0 : 1;
        int times = Integer.parseInt(message.metaOrDefault(MqConstants.MQ_META_TIMES, "0"));
        long expiration = Long.parseLong(message.metaOrDefault(MqConstants.MQ_META_EXPIRATION, "0"));
        String partition = message.meta(MqConstants.MQ_META_PARTITION);
        long scheduled = Long.parseLong(message.metaOrDefault(MqConstants.MQ_META_SCHEDULED, "0"));
        boolean sequence = Integer.parseInt(message.metaOrDefault(MqConstants.MQ_META_SEQUENCE, "0")) == 1;

        if (scheduled == 0) {
            //默认为当前ms（相对于后面者，有个排序作用）
            scheduled = System.currentTimeMillis();
        }


        //取出所有订阅的主题消费者
        Set<String> topicConsumerSet = subscribeMap.get(topic);

        if (topicConsumerSet != null) {
            //避免遍历 Set 时，出现 add or remove 而异常
            List<String> topicConsumerList = new ArrayList<>(topicConsumerSet);

            for (String topicConsumer : topicConsumerList) {
                routingDo(topicConsumer, message, tid, qos, sequence, expiration, partition, times, scheduled);
            }
        }
    }

    /**
     * 执行路由
     */
    public void routingDo(String queueName, Message message, String tid, int qos, boolean sequence, long expiration, String partition, int times, long scheduled) {
        MqQueue queue = queueMap.get(queueName);

        if (queue != null) {
            MqMessageHolder messageHolder = new MqMessageHolder(queueName, queue.getConsumerGroup(), message, tid, qos, sequence, expiration, partition, times, scheduled);
            queue.add(messageHolder);
        }
    }

    /**
     * 执行取消路由
     */
    public void unRoutingDo(Message message) {
        String tid = message.meta(MqConstants.MQ_META_TID);
        //可能是非法消息
        if (StrUtils.isEmpty(tid)) {
            log.warn("The tid cannot be null, sid={}", message.sid());
            return;
        }

        //复用解析
        String topic = message.meta(MqConstants.MQ_META_TOPIC);

        //取出所有订阅的主题消费者
        Set<String> topicConsumerSet = subscribeMap.get(topic);

        if (topicConsumerSet != null) {
            //避免遍历 Set 时，出现 add or remove 而异常
            List<String> topicConsumerList = new ArrayList<>(topicConsumerSet);

            for (String topicConsumer : topicConsumerList) {
                MqQueue queue = queueMap.get(topicConsumer);
                queue.removeAt(tid);
            }
        }
    }

    /**
     * 执行派发
     */
    protected void distributeDo() {
        while (!distributeThread.isInterrupted()) {
            try {
                int count = 0;

                List<MqQueue> queueList = new ArrayList<>(queueMap.values());
                for (MqQueue queue : queueList) {
                    try {
                        if (queue.distribute()) {
                            count++;
                        }
                    } catch (Throwable e) {
                        if (log.isWarnEnabled()) {
                            log.warn("MqQueue take error, queue={}", queue.getQueueName(), e);
                        }
                    }
                }

                if (count == 0) {
                    //一点消息都没有，就修复下
                    Thread.sleep(100);
                }
            } catch (Throwable e) {
                if (e instanceof InterruptedException == false) {
                    if (log.isWarnEnabled()) {
                        log.warn("MqQueue distribute error", e);
                    }
                }
            }
        }

        if (log.isWarnEnabled()) {
            log.warn("MqQueue take stoped!");
        }
    }
}