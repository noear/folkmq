package org.noear.folkmq.server;

import org.noear.socketd.transport.core.Session;

import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.LongAdder;

/**
 * @author noear
 * @since 1.0
 */
public abstract class MqTopicConsumerQueueBase implements MqTopicConsumerQueue {
    private final Object SESSION_LOCK = new Object();

    //用户会话（多个）
    private final List<Session> consumerSessions = new Vector<>();
    //消息计数器
    private final LongAdder[] messageCounters = new LongAdder[9];

    public MqTopicConsumerQueueBase() {
        //初始化计数器
        for (int i = 0; i < messageCounters.length; i++) {
            messageCounters[i] = new LongAdder();
        }
    }

    /**
     * 消息计数加数
     */
    public void messageCounterAdd(MqMessageHolder mh) {
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
    public void messageCounterSub(MqMessageHolder mh) {
        int n = mh.getDistributeCount();
        if (n > 7) {
            messageCounters[8].decrement();
        } else {
            messageCounters[n].decrement();
        }
    }

    /**
     * 消息计数器
     */
    public long messageCounter(int n) {
        if (n > 7) {
            return messageCounters[8].longValue();
        } else if (n < 1) {
            return messageCounters[0].longValue();
        } else {
            return messageCounters[n].longValue();
        }
    }


    /**
     * 会话数量
     */
    public int sessionCount() {
        return consumerSessions.size();
    }

    /**
     * 添加消费者会话
     */
    @Override
    public void addSession(Session session) {
        consumerSessions.add(session);
    }

    /**
     * 移除消费者会话
     */
    @Override
    public void removeSession(Session session) {
        //removeSession 可能会对 get(idx) 带来安全，所以锁一下
        synchronized (SESSION_LOCK) {
            consumerSessions.remove(session);
        }
    }

    //单线程的
    private int sessionRoundIdx;

    public Session getSession() {
        //removeSession 可能会对 get(idx) 带来安全，所以锁一下

        synchronized (SESSION_LOCK) {
            int idx = 0;
            if (consumerSessions.size() > 1) {
                //使用轮询
                sessionRoundIdx++;
                idx = sessionRoundIdx % consumerSessions.size();
                if (sessionRoundIdx > 999_999_999) {
                    sessionRoundIdx = 0;
                }
            }

            return consumerSessions.get(idx);
        }
    }

    @Override
    public void close() {
        consumerSessions.clear();
    }
}