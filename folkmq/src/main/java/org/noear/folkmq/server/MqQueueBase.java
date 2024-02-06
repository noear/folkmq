package org.noear.folkmq.server;

import org.noear.socketd.transport.core.EntityMetas;
import org.noear.socketd.transport.core.Session;
import org.noear.socketd.utils.StrUtils;

import java.util.Collection;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 队列基类
 *
 * @author noear
 * @since 1.0
 */
public abstract class MqQueueBase implements MqQueue {
    //会话操作锁
    private final ReentrantLock SESSION_LOCK = new ReentrantLock(true);

    //消费者会话列表
    private final List<Session> consumerSessions = new Vector<>();
    //消息计数器
    private final LongAdder[] messageCounters = new LongAdder[9];

    public MqQueueBase() {
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
        //removeSession 可能会对 get(idx) 带来安全，所以锁一下
        SESSION_LOCK.lock();

        try {
            consumerSessions.remove(session);
        } finally {
            SESSION_LOCK.unlock();
        }
    }

    @Override
    public Collection<Session> getSessions(){
        return consumerSessions;
    }

    //在锁内执行（是线程安全的）
    private int sessionRoundIdx;

    /**
     * 获取一个会话（轮询负载均衡）
     * */
    protected Session getSessionOne(MqMessageHolder messageHolder) {
        //removeSession 可能会对 get(idx) 带来安全，所以锁一下

        SESSION_LOCK.lock();

        try {
            int idx = 0;
            if (consumerSessions.size() > 1) {
                if(messageHolder.getAtName() != null && messageHolder.getAtName().endsWith("!")) {
                    //尝试 ip_hash
                    String ip = messageHolder.getContent().meta(EntityMetas.META_X_REAL_IP);
                    if (StrUtils.isNotEmpty(ip)) {
                        idx = ip.hashCode() & consumerSessions.size();
                        return consumerSessions.get(idx);
                    }
                }

                //使用轮询
                sessionRoundIdx++;
                idx = sessionRoundIdx % consumerSessions.size();
                if (sessionRoundIdx > 999_999) {
                    sessionRoundIdx = 0;
                }
            }

            return consumerSessions.get(idx);
        }finally {
            SESSION_LOCK.unlock();
        }
    }

    @Override
    public void close() {
        consumerSessions.clear();
    }
}