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
 * 队列默认实现
 *
 * @author noear
 * @since 1.0
 */
public class MqQueueDefault extends MqQueueBase implements MqQueue {
    private static final Logger log = LoggerFactory.getLogger(MqQueueDefault.class);

    //主题
    private final String topic;
    //消费者组
    private final String consumerGroup;
    //队列名字 //queueName='topic#consumer'
    private final String queueName;

    //观察者（由上层传入）
    private final MqWatcher watcher;

    //消息字典
    private final Map<String, MqMessageHolder> messageMap;

    //消息队列与处理线程
    private final DelayQueue<MqMessageHolder> messageQueue;
    private final Thread messageQueueThread;

    public MqQueueDefault(MqWatcher watcher, String topic, String consumerGroup, String queueName) {
        super();
        this.topic = topic;
        this.consumerGroup = consumerGroup;
        this.queueName = queueName;

        this.watcher = watcher;

        this.messageMap = new ConcurrentHashMap<>();

        this.messageQueue = new DelayQueue<>();
        this.messageQueueThread = new Thread(this::queueTake);
        this.messageQueueThread.start();
    }

    //单线程计数
    private long messageQueueTakeRef = 0;

    private void queueTake() {
        while (!messageQueueThread.isInterrupted()) {
            try {
                MqMessageHolder messageHolder = messageQueue.poll();

                if (messageHolder != null) {
                    messageQueueTakeRef = 0;
                    messageCountSub(messageHolder);
                    distribute(messageHolder);
                } else {
                    if ((messageQueueTakeRef++) > 1000) {
                        if (log.isDebugEnabled()) {
                            log.debug("MqQueue take as null *1000, queue={}#{}", topic, consumerGroup);
                        }
                        messageQueueTakeRef = 0;
                    }

                    Thread.sleep(100);
                }
            } catch (Throwable e) {
                if (log.isWarnEnabled()) {
                    log.warn("MqQueue take error, queue={}#{}", topic, consumerGroup, e);
                }
            }
        }

        if (log.isWarnEnabled()) {
            log.warn("MqQueue take stoped!");
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
     * 获取消费者组
     */
    @Override
    public String getConsumerGroup() {
        return consumerGroup;
    }

    /**
     * 获取主题消费者
     * */
    public String getQueueName() {
        return queueName;
    }

    /**
     * 状态
     * */
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

        messageCountAdd(messageHolder);
    }

    private void internalAdd(MqMessageHolder mh) {
        messageQueue.add(mh);
        messageCountAdd(mh);
    }

    private void internalRemove(MqMessageHolder mh) {
        if (messageQueue.remove(mh)) {
            messageCountSub(mh);
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

        //如果有会话
        if (sessionCount() > 0) {
            //::派发
            try {
                distributeDo(messageHolder);
            } catch (Throwable e) {
                //进入延后队列
                internalRemove(messageHolder);
                internalAdd(messageHolder.delayed());

                //记日志
                if (log.isWarnEnabled()) {
                    log.warn("MqQueue distribute error, tid={}",
                            messageHolder.getTid(), e);
                }
            }
        } else {
            //::进入延后队列
            internalAdd(messageHolder.delayed());

            //记日志
            if (log.isDebugEnabled()) {
                log.debug("MqQueue distribute: @{} no sessions, times={}, tid={}",
                        consumerGroup,
                        messageHolder.getDistributeCount(),
                        messageHolder.getTid());
            }
        }
    }

    /**
     * 派发执行
     */
    private void distributeDo(MqMessageHolder messageHolder) throws IOException {
        //获取一个会话（轮询负载均衡）

        Session s1 = getSession();

        //观察者::派发时（在元信息调整之后，再观察）
        watcher.onDistribute(topic, consumerGroup, messageHolder);

        if (messageHolder.getQos() > 0) {
            //::Qos1

            //添加延时任务：2小时后，如果没有回执就重发（即消息最长不能超过2小时）
            messageHolder.setDistributeTime(System.currentTimeMillis() + MqNextTime.getMaxDelayMillis());
            internalAdd(messageHolder);

            //给会话发送消息
            s1.sendAndRequest(MqConstants.MQ_EVENT_DISTRIBUTE, messageHolder.getContent(), m -> {
                int ack = Integer.parseInt(m.metaOrDefault(MqConstants.MQ_META_ACK, "0"));
                acknowledgeDo(messageHolder, ack);
            });
        } else {
            //::Qos0
            s1.send(MqConstants.MQ_EVENT_DISTRIBUTE, messageHolder.getContent());
            //观察者::回执时
            watcher.onAcknowledge(topic, consumerGroup, messageHolder, true);

            messageMap.remove(messageHolder.getTid());

            //移除前，不要改属性
            messageHolder.setDone(true);
        }
    }

    private void acknowledgeDo(MqMessageHolder messageHolder, int ack) {
        //观察者::回执时
        watcher.onAcknowledge(topic, consumerGroup, messageHolder, ack > 0);

        if (ack > 0) {
            //ok
            messageMap.remove(messageHolder.getTid());
            internalRemove(messageHolder);

            //移除前，不要改移性
            messageHolder.setDone(true);
        } else {
            //no （尝试移除，再添加）//否则排序可能不会触发
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