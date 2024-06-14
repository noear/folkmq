package org.noear.folkmq.borker;

import org.noear.socketd.cluster.LoadBalancer;
import org.noear.socketd.transport.core.Session;
import org.noear.socketd.utils.StrUtils;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 队列基类
 *
 * @author noear
 * @since 1.0
 */
public abstract class MqQueueBase implements MqQueue {
    //消息字典
    protected final MqMessageHolderMap messageMap;
    //消息队列与处理线程
    protected final MqMessageHolderQueue messageQueue;
    //消息最后派发时间
    protected final AtomicLong messageDistributeTime = new AtomicLong(0);
    //消息索引器
    protected final AtomicLong messageIndexer = new AtomicLong();
    //消息添加锁（公平锁）
    protected final ReentrantLock messageAddLock = new ReentrantLock(true);

    //消费者会话列表
    private final List<Session> consumerSessions = new CopyOnWriteArrayList<>();

    public MqQueueBase() {
        this.messageMap = new MqMessageHolderMap();
        this.messageQueue = new MqMessageHolderQueue();
    }

    /**
     * 获取消息计数
     */
    public long messageCount(int n) {
        return messageQueue.countGet(n);
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
    public void sessionAdd(Session session) {
        if (consumerSessions.contains(session) == false) {
            consumerSessions.add(session);
        }
    }

    /**
     * 移除消费者会话
     */
    @Override
    public void sessionRemove(Session session) {
        consumerSessions.remove(session);
    }

    @Override
    public Collection<Session> sessionAll() {
        return consumerSessions;
    }

    /**
     * 获取一个会话（轮询负载均衡）
     */
    protected Session sessionGetOne(MqMessageHolder messageHolder) {
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
    @Override
    public int messageTotal() {
        return messageMap.size();
    }

    /**
     * 消息总量2（用于做校验）
     */
    @Override
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