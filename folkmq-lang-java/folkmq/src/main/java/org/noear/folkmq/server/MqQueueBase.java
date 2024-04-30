package org.noear.folkmq.server;

import org.noear.socketd.cluster.LoadBalancer;
import org.noear.socketd.transport.core.Session;
import org.noear.socketd.utils.StrUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 队列基类
 *
 * @author noear
 * @since 1.0
 */
public abstract class MqQueueBase implements MqQueue {
    //消息字典
    protected final Map<String, MqMessageHolder> messageMap;
    //消息队列与处理线程
    protected final DelayQueue<MqMessageHolder> messageQueue;
    //消息最后派发时间
    protected final AtomicLong messageDistributeTime = new AtomicLong(0);
    //消息索引器
    protected final AtomicLong messageIndexer = new AtomicLong();
    //消息添加锁（公平锁）
    protected final ReentrantLock messageAddLock = new ReentrantLock(true);

    //消费者会话列表
    private final List<Session> consumerSessions = new CopyOnWriteArrayList<>();
    //消息计数器
    private final LongAdder[] messageCounters = new LongAdder[9];

    public MqQueueBase() {
        this.messageMap = new ConcurrentHashMap<>();
        this.messageQueue = new DelayQueue<>();

        //初始化计数器
        for (int i = 0; i < messageCounters.length; i++) {
            messageCounters[i] = new LongAdder();
        }
    }

    /**
     * 消息计数加数
     */
    public void messageCountAdd(MqMessageHolder mh) {
        int n = mh.getDistributeCount();

        if (n > 7) {
            messageCounters[8].increment();
        } else {
            messageCounters[n].increment();
        }
    }

    /**
     * 消息计数减数
     */
    public void messageCountSub(MqMessageHolder mh) {
        int n = mh.getDistributeCount();

        if (n > 7) {
            messageCounters[8].decrement();
        } else {
            messageCounters[n].decrement();
        }
    }

    /**
     * 获取消息计数
     */
    public long messageCount(int n) {
        if (n > 7) {
            return messageCounters[8].longValue();
        } else {
            return messageCounters[n].longValue();
        }
    }


    /**
     * 消费者会话数量
     */
    public int sessionCount() {
        return consumerSessions.size();
    }

    /**
     * 添加消费者会话
     */
    @Override
    public void addSession(Session session) {
        if (consumerSessions.contains(session) == false) {
            consumerSessions.add(session);
        }
    }

    /**
     * 移除消费者会话
     */
    @Override
    public void removeSession(Session session) {
        consumerSessions.remove(session);
    }

    @Override
    public Collection<Session> getSessions() {
        return consumerSessions;
    }

    /**
     * 获取一个会话（轮询负载均衡）
     */
    protected Session getSessionOne(MqMessageHolder messageHolder) {
        if (messageHolder.isSequence()) {
            if (StrUtils.isEmpty(messageHolder.getSequenceSharding())) {
                return LoadBalancer.getAnyByHash(consumerSessions, getTopic());
            } else {
                return LoadBalancer.getAnyByHash(consumerSessions, messageHolder.getSequenceSharding());
            }
        } else {
            return LoadBalancer.getAnyByPoll(consumerSessions);
        }
    }

    /**
     * 消息总量
     */
    public int messageTotal() {
        return messageMap.size();
    }

    /**
     * 消息总量2（用于做校验）
     */
    public int messageTotal2() {
        return messageQueue.size();
    }

    @Override
    public void close() {
        consumerSessions.clear();

        messageQueue.clear();
        messageMap.clear();
    }
}