package org.noear.folkmq.broker;

import org.noear.folkmq.common.MqConstants;
import org.noear.folkmq.common.MqMetasResolver;
import org.noear.socketd.transport.core.Message;
import org.noear.socketd.transport.core.Session;
import org.noear.socketd.transport.core.listener.EventListener;
import org.noear.socketd.utils.StrUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author noear
 * @since 1.1
 */
public abstract class MqBorkerListenerBase extends EventListener implements MqBorkerInternal {
    protected static final Logger log = LoggerFactory.getLogger(MqBorkerListener.class);


    //观察者
    protected MqWatcher watcher;
    //代理模式（即连接代理的集群模式）
    protected boolean proxyMode;
    //订阅锁
    protected final ReentrantLock subscribeLock = new ReentrantLock(true);
    //所有会话
    protected final Map<String, Session> sessionAllMap = new ConcurrentHashMap<>();
    //服务端访问账号
    protected final Map<String, String> serverAccessMap = new ConcurrentHashMap<>();

    //订阅关系(topic=>[queueName]) //queueName='topic#consumer'
    protected final Map<String, Set<String>> subscribeMap = new ConcurrentHashMap<>();
    //队列字典(queueName=>Queue)
    protected final Map<String, MqQueue> queueMap = new ConcurrentHashMap<>();
    //事务消息
    protected final Map<String, String> transactionMessageMap = new ConcurrentHashMap<>();

    //派发线程
    protected Thread distributeThread;

    protected final AtomicBoolean isStarted = new AtomicBoolean(false);

    /**
     * 通道类型
     * */
    public String chanelType() {
        if (proxyMode) {
            return "proxy";
        } else {
            return "client";
        }
    }

    /**
     * 获取所有会话
     */
    @Override
    public Collection<Session> getSessionAll() {
        return sessionAllMap.values();
    }

    /**
     * 获取所有会话数量
     */
    @Override
    public int getSessionCount() {
        return sessionAllMap.size();
    }

    /**
     * 获取订阅集合
     */
    @Override
    public Map<String, Set<String>> getSubscribeMap() {
        return Collections.unmodifiableMap(subscribeMap);
    }

    @Override
    public boolean hasSubscribe(String topic) {
        return subscribeMap.containsKey(topic);
    }

    /**
     * 获取队列集合
     */
    @Override
    public Map<String, MqQueue> getQueueMap() {
        return Collections.unmodifiableMap(queueMap);
    }

    /**
     * 获取队列
     */
    @Override
    public MqQueue getQueue(String queueName) {
        return queueMap.get(queueName);
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

        subscribeLock.lock();

        try {
            //::1.构建订阅关系，并获取队列
            MqQueue queue = queueGetOrInit(topic, consumerGroup, queueName);


            //::2.标识会话身份（从持久层恢复时，会话可能为 null）

            if (session != null) {
                log.info("Broker: {} channel subscribe topic={}, consumerGroup={}, sessionId={}", chanelType(), topic, consumerGroup, session.sessionId());

                //会话绑定队列（可以绑定多个队列）
                session.attrPut(queueName, "1");

                //加入队列会话
                queue.sessionAdd(session);
            }
        } finally {
            subscribeLock.unlock();
        }
    }

    protected MqQueue queueGetOrInit(String topic, String consumerGroup, String queueName) {
        //建立订阅关系(topic=>[queueName]) //queueName='topic#consumer'
        Set<String> queueNameSet = subscribeMap.computeIfAbsent(topic, n -> Collections.newSetFromMap(new ConcurrentHashMap<>()));
        queueNameSet.add(queueName);

        //队列映射关系(queueName=>Queue)
        MqQueue queue = queueMap.get(queueName);
        if (queue == null) {
            queue = new MqQueueDefault((MqBorkerListener) this, watcher, topic, consumerGroup, queueName);
            queueMap.put(queueName, queue);
        }

        return queue;
    }

    /**
     * 执行取消订阅
     */
    @Override
    public void unsubscribeDo(String topic, String consumerGroup, Session session) {
        if (session == null) {
            return;
        }

        log.info("Broker: {} channel unsubscribe topic={}, consumerGroup={}, sessionId={}", chanelType(), topic, consumerGroup, session.sessionId());

        String queueName = topic + MqConstants.SEPARATOR_TOPIC_CONSUMER_GROUP + consumerGroup;

        //1.获取相关队列
        MqQueue queue = queueMap.get(queueName);

        //2.移除队列绑定
        session.attrMap().remove(queueName);

        //3.退出队列会话
        if (queue != null) {
            queue.sessionRemove(session);
        }
    }

    /**
     * 执行路由
     */
    @Override
    public void routingDo(MqMetasResolver mr, Message message) {
        //复用解析
        MqDraft draft = new MqDraft(mr, message);

        //取出所有订阅的主题消费者
        Set<String> topicConsumerSet = subscribeMap.get(draft.topic);

        if (topicConsumerSet != null) {
            //避免遍历 Set 时，出现 add or remove 而异常
            List<String> topicConsumerList = new ArrayList<>(topicConsumerSet);

            for (String topicConsumer : topicConsumerList) {
                MqQueue queue = queueMap.get(topicConsumer);
                if (queue == null || queue.isTransaction()) {
                    continue;
                }

                routingToQueueDo(draft, queue);
            }
        }
    }

    protected void routingToQueueName(MqDraft draft, String queueName) {
        //取出所有订阅的主题消费者
        MqQueue queue = queueMap.get(queueName);

        routingToQueueDo(draft, queue);
    }

    /**
     * 执行路由
     */
    public void routingToQueueDo(MqDraft draft, MqQueue queue) {
        if (queue != null) {
            MqMessageHolder messageHolder = new MqMessageHolder(draft, queue.getQueueName(), queue.getConsumerGroup());
            queue.add(messageHolder);
        }
    }

    /**
     * 执行取消路由
     */
    public void unRoutingDo(Message message) {
        String key = message.meta(MqConstants.MQ_META_KEY);
        //可能是非法消息
        if (StrUtils.isEmpty(key)) {
            log.warn("Broker: message key cannot be null, sid={}", message.sid());
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
                queue.removeAt(key);
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

                if (isStarted.get()) {
                    List<MqQueue> queueList = new ArrayList<>(queueMap.values());
                    for (MqQueue queue : queueList) {
                        try {
                            if (queue.distribute()) {
                                count++;
                            }
                        } catch (Throwable e) {
                            if (log.isWarnEnabled()) {
                                log.warn("Broker: queue take error, queue={}", queue.getQueueName(), e);
                            }
                        }
                    }
                }

                if (count == 0) {
                    //一点消息都没有，就修复下
                    Thread.sleep(10);
                }
            } catch (Throwable e) {
                if (e instanceof InterruptedException == false) {
                    if (log.isWarnEnabled()) {
                        log.warn("Broker: queue distribute error", e);
                    }
                }
            }
        }

        if (log.isWarnEnabled()) {
            log.warn("Broker: queue take stoped!");
        }
    }
}