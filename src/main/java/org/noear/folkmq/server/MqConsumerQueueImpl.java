package org.noear.folkmq.server;

import org.noear.folkmq.MqConstants;
import org.noear.socketd.transport.core.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.DelayQueue;

/**
 * 消费者队列（一个消费者一个队列，一个消费者可多个会话）
 *
 * @author noear
 * @since 1.0
 */
public class MqConsumerQueueImpl implements MqConsumerQueue {
    private static final Logger log = LoggerFactory.getLogger(MqConsumerQueueImpl.class);

    //用户
    private final String consumer;
    //用户会话（多个）
    private final List<Session> consumerSessions;
    //持久化
    private final MqPersistent persistent;


    protected final DelayQueue<MqMessageHolder> queue = new DelayQueue<>();
    private final Thread thread;

    public MqConsumerQueueImpl(MqPersistent persistent,String consumer) {
        this.persistent = persistent;
        this.consumer = consumer;
        this.consumerSessions = new ArrayList<>();

        thread = new Thread(this::queueTake);
        thread.start();
    }

    private void queueTake() {
        while (!thread.isInterrupted()) {
            try {
                MqMessageHolder messageHolder = queue.take();
                distribute(messageHolder);
            } catch (Exception e) {
                if (log.isWarnEnabled()) {
                    log.warn("MqConsumerQueue queueTake error", e);
                }
            }
        }
    }

    /**
     * 获取消费者
     */
    public String getConsumer() {
        return consumer;
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
        consumerSessions.remove(session);
    }

    /**
     * 添加消息
     */
    @Override
    public void add(MqMessageHolder messageHolder) {
        if(messageHolder.isDone()){
            return;
        }

        queue.add(messageHolder);

        //distribute(messageHolder);
    }

    /**
     * 执行派发
     */
    protected void distribute(MqMessageHolder messageHolder) {
        //找到此身份的其中一个会话（如果是 ip 就一个；如果是集群名则任选一个）
        if (consumerSessions.size() > 0) {
            try {
                distributeDo(messageHolder, consumerSessions);
            } catch (Throwable e) {
                //进入延后队列
                addDelayed(messageHolder.delayed());
            }
        } else {
            //进入延后队列
            addDelayed(messageHolder.delayed());

            //记日志
            if (log.isWarnEnabled()) {
                log.warn("MqConsumerQueue distribute: no sessions!");
            }
        }
    }

    /**
     * 派发执行
     */
    private void distributeDo(MqMessageHolder messageHolder, List<Session> sessions) throws IOException {
        //随机取一个会话（集群会有多个会话，实例有时也会有多个会话）
        int idx = 0;
        if (sessions.size() > 1) {
            idx = new Random().nextInt(sessions.size());
        }
        Session s1 = sessions.get(idx);

        //设置新的派发次数
        messageHolder.getContent()
                .meta(MqConstants.MQ_META_TIMES, String.valueOf(messageHolder.getDistributeCount()));


        //持久化::派发时（在元信息调整之后，持久化）
        persistent.onDistribute(consumer, messageHolder);

        //添加延时任务：2小时后，如果没有回执就重发（即消息最长不能超过2小时）
        addDelayed(messageHolder, MqNextTime.getMaxDelayMillis());

        //给会话发送消息
        s1.sendAndSubscribe(MqConstants.MQ_EVENT_DISTRIBUTE, messageHolder.getContent(), m -> {
            int ack = Integer.parseInt(m.metaOrDefault(MqConstants.MQ_META_ACK, "0"));

            //持久化::回执时
            persistent.onAcknowledge(consumer, messageHolder, ack == 1);

            if (ack == 0) {
                //no （如果在队列改时间即可；如果不在队列说明有补发过）
                messageHolder.delayed();
            } else {
                //ok
                messageHolder.setDone(true);
                queue.remove(messageHolder);
            }
        });
    }

    /**
     * 添加延时处理
     */
    protected void addDelayed(MqMessageHolder messageHolder) {
        queue.add(messageHolder);
    }

    /**
     * 添加延时处理
     *
     * @param millisDelay 延时（单位：毫秒）
     */
    protected void addDelayed(MqMessageHolder messageHolder, long millisDelay) {
        messageHolder.setDistributeTime(System.currentTimeMillis() + millisDelay);
        queue.add(messageHolder);
    }

    @Override
    public void close() throws IOException {
        if(thread != null){
            thread.interrupt();
        }
    }
}