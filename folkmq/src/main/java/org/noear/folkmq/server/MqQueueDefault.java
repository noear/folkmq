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

    public MqQueueDefault(MqWatcher watcher, String topic, String consumerGroup, String queueName) {
        super();
        this.topic = topic;
        this.consumerGroup = consumerGroup;
        this.queueName = queueName;

        this.watcher = watcher;

        this.messageMap = new ConcurrentHashMap<>();

        this.messageQueue = new DelayQueue<>();
    }

    /**
     * 提取
     */
    @Override
    public boolean distribute() {
        MqMessageHolder messageHolder = messageQueue.poll();

        if (messageHolder != null) {
            distribute0(messageHolder);
            return true;
        } else {
            return false;
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
     */
    public String getQueueName() {
        return queueName;
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

    /**
     * 移除消息
     */
    @Override
    public void removeAt(String tid) {
        MqMessageHolder messageHolder = messageMap.remove(tid);
        if (messageHolder != null) {
            internalRemove(messageHolder);
        }
    }

    private void internalAdd(MqMessageHolder mh) {
        messageQueue.add(mh);
        messageCountAdd(mh);
    }

    private void internalRemove(MqMessageHolder mh) {
        messageQueue.remove(mh);
        messageCountSub(mh);
    }

    /**
     * 强制清空
     */
    @Override
    public void forceClear() {
        messageMap.clear();
        messageQueue.clear();
    }

    /**
     * 强制派发
     */
    @Override
    public void forceDistribute(int times, int count) {
        if (count == 0 || count > messageTotal()) {
            count = messageTotal();
        }

        List<MqMessageHolder> msgList = new ArrayList<>(count);

        Iterator<Map.Entry<String, MqMessageHolder>> iterator = messageMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, MqMessageHolder> kv = iterator.next();
            MqMessageHolder msg = kv.getValue();
            if (msg.getDistributeCount() >= times && msg.isDone() == false) {
                msgList.add(msg);
            }

            if (msgList.size() == count) {
                break;
            }
        }

        for (MqMessageHolder msg : msgList) {
            messageQueue.remove(msg);
            msg.setDistributeTime(System.currentTimeMillis());
            messageQueue.add(msg);
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

    /**
     * 执行派发
     */
    protected void distribute0(MqMessageHolder messageHolder) {
        messageCountSub(messageHolder);

        if (messageHolder.isDone()) {
            messageMap.remove(messageHolder.getTid());
            return;
        }

        //如果有会话
        if (sessionCount() > 0) {
            //获取一个会话（轮询负载均衡）
            Session s1 = getSession();

            //::派发
            try {
                distributeDo(s1, messageHolder);
            } catch (Throwable e) {
                //如果无效，则移掉
                if(s1.isValid() == false){
                    removeSession(s1);
                }

                //进入延后队列
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
    private void distributeDo(Session s1, MqMessageHolder messageHolder) throws IOException {
        //观察者::派发时（在元信息调整之后，再观察）
        watcher.onDistribute(topic, consumerGroup, messageHolder);

        if (messageHolder.getQos() > 0) {
            //::Qos1

            //1.给会话发送消息 //如果有异步，上面会加入队列
            s1.sendAndRequest(MqConstants.MQ_EVENT_DISTRIBUTE, messageHolder.getContent(), -1).thenReply(r -> {
                int ack = Integer.parseInt(r.metaOrDefault(MqConstants.MQ_META_ACK, "0"));
                acknowledgeDo(messageHolder, ack, true);
            });

            //2.添加保险延时任务：如果没有回执就重发
            messageHolder.setDistributeTime(System.currentTimeMillis() + MqNextTime.maxConsumeMillis());
            internalAdd(messageHolder);
        } else {
            //::Qos0
            s1.send(MqConstants.MQ_EVENT_DISTRIBUTE, messageHolder.getContent());
            acknowledgeDo(messageHolder, 1, false);
        }
    }

    private void acknowledgeDo(MqMessageHolder messageHolder, int ack, boolean removeQueue) {
        //观察者::回执时
        watcher.onAcknowledge(topic, consumerGroup, messageHolder, ack > 0);

        if (ack > 0) {
            //ok
            messageMap.remove(messageHolder.getTid());
            if (removeQueue) {
                internalRemove(messageHolder);
            }
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
        messageQueue.clear();
        messageMap.clear();
        super.close();
    }
}