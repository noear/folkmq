package org.noear.folkmq.server;

import org.noear.folkmq.common.MqConstants;
import org.noear.socketd.transport.core.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class MqTopicConsumerQueueDefault extends MqTopicConsumerQueueBase implements MqTopicConsumerQueue {
    private static final Logger log = LoggerFactory.getLogger(MqTopicConsumerQueueDefault.class);

    //主题
    private final String topic;
    //用户
    private final String consumer;
    //观察者
    private final MqWatcher watcher;


    //消息字典
    private final Map<String, MqMessageHolder> messageMap;

    //消息队列与处理线程
    private final DelayQueue<MqMessageHolder> messageQueue;
    private final Thread messageQueueThread;

    public MqTopicConsumerQueueDefault(MqWatcher watcher, String topic, String consumer) {
        super();
        this.watcher = watcher;
        this.topic = topic;
        this.consumer = consumer;

        this.messageMap = new ConcurrentHashMap<>();

        this.messageQueue = new DelayQueue<>();
        this.messageQueueThread = new Thread(this::queueTake);
        this.messageQueueThread.start();
    }

    //单线程计数
    private long queueTakeRef = 0;

    private void queueTake() {
        while (!messageQueueThread.isInterrupted()) {
            try {
                MqMessageHolder messageHolder = messageQueue.poll();

                if (messageHolder != null) {
                    queueTakeRef = 0;
                    messageCounterSub(messageHolder);
                    distribute(messageHolder);
                } else {
                    if ((queueTakeRef++) > 1000) {
                        log.info("MqConsumerQueue queueTake as null *1000, queue={}#{}", topic, consumer);
                        queueTakeRef = 0;
                    }

                    Thread.sleep(100);
                }
            } catch (Throwable e) {
                if (log.isWarnEnabled()) {
                    log.warn("MqConsumerQueue queueTake error, queue={}#{}", topic, consumer, e);
                }
            }
        }

        if (log.isWarnEnabled()) {
            log.warn("MqConsumerQueue queueTake stoped!");
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

    public boolean isAlive() {
        return messageQueueThread.isAlive();
    }

    public Thread.State state() {
        return messageQueueThread.getState();
    }

    /**
     * 获取消息表
     */
    public Map<String, MqMessageHolder> getMessageMap() {
        return Collections.unmodifiableMap(messageMap);
    }


    /**
     * 添加消息
     */
    @Override
    public void add(MqMessageHolder messageHolder) {
        messageMap.put(messageHolder.getTid(), messageHolder);
        messageQueue.add(messageHolder);

        messageCounterAdd(messageHolder);
    }

    private void internalAdd(MqMessageHolder mh) {
        messageQueue.add(mh);
        messageCounterAdd(mh);
    }

    private void internalRemove(MqMessageHolder mh) {
        if(messageQueue.remove(mh)){
            messageCounterSub(mh);
        }
    }

    /**
     * 消息总量
     */
    public int messageTotal() {
        return messageQueue.size();
    }

    /**
     * 消息总量2（用于做校验）
     */
    public int messageTotal2() {
        return messageMap.size();
    }

    /**
     * 执行派发
     */
    protected void distribute(MqMessageHolder messageHolder) {
        if (messageHolder.isDone()) {
            messageMap.remove(messageHolder.getTid());
            return;
        }

        //找到此身份的其中一个会话（如果是 ip 就一个；如果是集群名则任选一个）
        if (sessionCount() > 0) {
            try {
                distributeDo(messageHolder);
            } catch (Throwable e) {
                //进入延后队列
                internalRemove(messageHolder);
                internalAdd(messageHolder.delayed());

                //记日志
                if (log.isWarnEnabled()) {
                    log.warn("MqConsumerQueue distribute error, tid={}",
                            messageHolder.getTid(), e);
                }
            }
        } else {
            //进入延后队列
            internalAdd(messageHolder.delayed());

            //记日志
            if (log.isWarnEnabled()) {
                log.warn("MqConsumerQueue distribute: @{} no sessions, times={}, tid={}",
                        consumer,
                        messageHolder.getDistributeCount(),
                        messageHolder.getTid());
            }
        }
    }

    /**
     * 派发执行
     */
    private void distributeDo(MqMessageHolder messageHolder) throws IOException {
        //随机取一个会话（集群会有多个会话，实例有时也会有多个会话）

        Session s1 = getSession();

        //观察者::派发时（在元信息调整之后，再观察）
        watcher.onDistribute(consumer, messageHolder);

        if (messageHolder.getQos() > 0) {
            //::Qos1

            //添加延时任务：2小时后，如果没有回执就重发（即消息最长不能超过2小时）
            messageHolder.setDistributeTime(System.currentTimeMillis() + MqNextTime.getMaxDelayMillis());
            internalAdd(messageHolder);

            //给会话发送消息 //用 sendAndSubscribe 不安全，时间太久可能断连过（流就不能用了）
            s1.sendAndSubscribe(MqConstants.MQ_EVENT_DISTRIBUTE, messageHolder.getContent(), m -> {
                int ack = Integer.parseInt(m.metaOrDefault(MqConstants.MQ_META_ACK, "0"));
                acknowledgeDo(messageHolder, ack);
            });
        } else {
            //::Qos0
            s1.send(MqConstants.MQ_EVENT_DISTRIBUTE, messageHolder.getContent());
            //观察者::回执时
            watcher.onAcknowledge(consumer, messageHolder, true);

            messageMap.remove(messageHolder.getTid());

            //移除前，不要改移性
            messageHolder.setDone(true);
        }
    }

    private void acknowledgeDo(MqMessageHolder messageHolder, int ack) {
        //观察者::回执时
        watcher.onAcknowledge(consumer, messageHolder, ack > 0);

        if (ack > 0) {
            //ok
            messageMap.remove(messageHolder.getTid());
            internalRemove(messageHolder);

            //移除前，不要改移性
            messageHolder.setDone(true);
        } else {
            //no （如果在队列改时间即可；如果不在队列说明有补发过）
            internalRemove(messageHolder);
            internalAdd(messageHolder.delayed());
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
        super.close();
    }
}