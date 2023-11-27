package org.noear.folkmq.server;

import org.noear.folkmq.common.MqConstants;
import org.noear.socketd.transport.core.Message;
import org.noear.socketd.transport.core.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;

/**
 * 主题消费者队列默认实现（服务端给 [一个主题+一个消费者] 安排一个队列，一个消费者可多个会话，只随机给一个会话派发）
 *
 * @author noear
 * @since 1.0
 */
public class MqTopicConsumerQueueDefault implements MqTopicConsumerQueue {
    private static final Logger log = LoggerFactory.getLogger(MqTopicConsumerQueueDefault.class);

    //主题
    private final String topic;
    //用户
    private final String consumer;
    //用户会话（多个）
    private final List<Session> consumerSessions;
    //持久化
    private final MqPersistent persistent;


    //消息字典
    private final Map<String, MqMessageHolder> messageMap;

    //消息队列与处理线程
    private final DelayQueue<MqMessageHolder> messageQueue;
    private final Thread messageQueueThread;

    public MqTopicConsumerQueueDefault(MqPersistent persistent, String topic, String consumer) {
        this.persistent = persistent;
        this.topic = topic;
        this.consumer = consumer;
        this.consumerSessions = new ArrayList<>();

        this.messageMap = new ConcurrentHashMap<>();

        this.messageQueue = new DelayQueue<>();
        this.messageQueueThread = new Thread(this::queueTake);
        this.messageQueueThread.start();
    }

    private void queueTake() {
        while (!messageQueueThread.isInterrupted()) {
            try {
                MqMessageHolder messageHolder = messageQueue.take();
                distribute(messageHolder);
            } catch (Throwable e) {
                if (log.isWarnEnabled()) {
                    log.warn("MqConsumerQueue queueTake error", e);
                }
            }
        }
    }

    /**
     * 获取主题
     */
    @Override
    public String getTopic() {
        return topic;
    }

    /**
     * 获取消费者
     */
    @Override
    public String getConsumer() {
        return consumer;
    }

    /**
     * 获取消息表
     */
    @Override
    public Map<String, MqMessageHolder> getMessageMap() {
        return Collections.unmodifiableMap(messageMap);
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
        messageMap.put(messageHolder.getTid(), messageHolder);
        messageQueue.add(messageHolder);
    }

    /**
     * 消息数量
     */
    public int size() {
        return messageQueue.size();
    }

    /**
     * 执行派发
     */
    protected void distribute(MqMessageHolder messageHolder) {
        if (messageHolder.isDone()) {
            messageMap.remove(messageHolder.getTid());
            return;
        }

        //MDC.put("tid", messageHolder.getTid());

        //找到此身份的其中一个会话（如果是 ip 就一个；如果是集群名则任选一个）
        if (consumerSessions.size() > 0) {
            try {
                distributeDo(messageHolder, consumerSessions);
            } catch (Throwable e) {
                //进入延后队列
                messageQueue.remove(messageHolder);
                messageQueue.add(messageHolder.delayed());

                //记日志
                if (log.isWarnEnabled()) {
                    log.warn("MqConsumerQueue distribute error, tid={}",
                            messageHolder.getTid(), e);
                }
            }
        } else {
            //进入延后队列
            messageQueue.add(messageHolder.delayed());

            //记日志
            if (log.isWarnEnabled()) {
                log.warn("MqConsumerQueue distribute: no sessions, tid={}",
                        messageHolder.getTid());
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

        if (messageHolder.getQos() > 0) {
            //::Qos1

            //添加延时任务：2小时后，如果没有回执就重发（即消息最长不能超过2小时）
            messageHolder.setDistributeTime(System.currentTimeMillis() + MqNextTime.getMaxDelayMillis());
            messageQueue.add(messageHolder);

            //给会话发送消息 //用 sendAndSubscribe 不安全，时间太久可能断连过（流就不能用了）
            s1.send(MqConstants.MQ_EVENT_DISTRIBUTE, messageHolder.getContent());
        } else {
            //::Qos0
            s1.send(MqConstants.MQ_EVENT_DISTRIBUTE, messageHolder.getContent());
            //持久化::回执时
            persistent.onAcknowledge(consumer, messageHolder, true);

            messageMap.remove(messageHolder.getTid());

            //移除前，不要改移性
            messageHolder.setDone(true);
        }
    }

    @Override
    public void acknowledge(String tid, Message message) {
        int ack = Integer.parseInt(message.metaOrDefault(MqConstants.MQ_META_ACK, "0"));

        MqMessageHolder messageHolder = messageMap.get(tid);

        if (messageHolder == null) {
            if (log.isWarnEnabled()) {
                log.warn("Server {}#{} queue not found, tid={}", topic, consumer, tid);
            }
            return;
        }

        //持久化::回执时
        persistent.onAcknowledge(consumer, messageHolder, ack > 0);

        if (ack > 0) {
            //ok
            messageMap.remove(tid);
            messageQueue.remove(messageHolder);

            //移除前，不要改移性
            messageHolder.setDone(true);
        } else {
            //no （如果在队列改时间即可；如果不在队列说明有补发过）
            messageQueue.remove(messageHolder);
            messageQueue.add(messageHolder.delayed());
        }
    }

    /**
     * 关闭
     */
    @Override
    public void close() {
        if (messageQueueThread != null) {
            messageQueueThread.interrupt();
        }

        messageQueue.clear();
        messageMap.clear();

        consumerSessions.clear();
    }
}