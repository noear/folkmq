package org.noear.folkmq.broker;

import org.noear.folkmq.common.MqConstants;
import org.noear.socketd.transport.core.Message;
import org.noear.socketd.transport.core.Session;
import org.noear.socketd.utils.SessionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 队列默认实现
 *
 * @author noear
 * @since 1.0
 */
public class MqQueueDefault extends MqQueueBase implements MqQueue {
    private static final Logger log = LoggerFactory.getLogger(MqQueueDefault.class);

    //服务监听器
    private final MqBorkerListener serviceListener;

    //是否为事务缓存队列
    private final boolean transaction;
    private final AtomicReference<Boolean> sequenceLock = new AtomicReference<>(false);
    //主题
    private final String topic;
    //消费者组
    private final String consumerGroup;
    //队列名字 //queueName='topic#consumer'
    private final String queueName;

    //观察者（由上层传入）
    private final MqWatcher watcher;

    public MqQueueDefault(MqBorkerListener serviceListener, MqWatcher watcher, String topic, String consumerGroup, String queueName) {
        super();

        this.serviceListener = serviceListener;
        this.topic = topic;
        this.consumerGroup = consumerGroup;
        this.queueName = queueName;

        this.transaction = MqConstants.MQ_TRAN_CONSUMER_GROUP.equals(consumerGroup);

        this.watcher = watcher;
    }


    @Override
    public boolean isTransaction() {
        return transaction;
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
        messageAddLock.lock();

        try {
            if (messageHolder.getDistributeTime() != messageDistributeTime.get()) {
                //如果超过1秒的，理解为定时消息
                if (messageHolder.getDistributeTime() < System.currentTimeMillis() + 1_000) {
                    messageDistributeTime.set(messageHolder.getDistributeTime());
                    messageIndexer.set(0L);
                }
            }

            messageHolder.setDistributeIdx(messageIndexer.incrementAndGet());

            messageMap.put(messageHolder.getKey(), messageHolder);
            internalAdd(messageHolder);
        } finally {
            messageAddLock.unlock();
        }
    }

    /**
     * 提取
     */
    @Override
    public boolean distribute() {
        if (sequenceLock.get()) {
            //如果有顺序锁，暂停派送
            return true;
        }

        MqMessageHolder messageHolder;

        if (targetSessionCount() == 0) {
            //如果没有会话，则不派发（避免空转浪费）
            messageHolder = messageQueue.peek();
            if (messageHolder == null) {
                return false;
            }

            if (messageHolder.getExpiration() < 1L) {
                //没有过期时间
                return false;
            } else {
                if (messageHolder.getExpiration() > System.currentTimeMillis()) {
                    //过期时间未到
                    return false;
                }
            }
        }

        messageHolder = messageQueue.poll();

        if (messageHolder != null) {
            if (messageHolder.isTransaction()) {
                //转发
                return transpond0(messageHolder);
            } else {
                //派发
                return distribute0(messageHolder);
            }
        } else {
            return false;
        }
    }

    /**
     * 目标会话数量
     */
    protected int targetSessionCount() {
        if (isTransaction()) {
            return serviceListener.brokerListener.getSessionCount();
        } else {
            return sessionCount();
        }
    }

    /**
     * 移除消息
     */
    @Override
    public void removeAt(String key) {
        MqMessageHolder messageHolder = messageMap.get(key);
        if (messageHolder != null) {
            internalDelete(messageHolder);
            internalRemove(messageHolder);
        }
    }

    /**
     * 事务确认
     */
    @Override
    public void affirmAt(String key, boolean isRollback) {
        MqMessageHolder messageHolder = messageMap.get(key);

        if (messageHolder != null) {
            internalRemove(messageHolder);
            affirmAtDo(messageHolder, isRollback);
        }
    }

    /**
     * 事务确认处理
     */
    protected void affirmAtDo(MqMessageHolder messageHolder, boolean isRollback) {
        internalDelete(messageHolder);

        if (isRollback == false) {
            Message message = messageHolder.noTransaction();
            serviceListener.routingDo(messageHolder.mr, message);
        }
    }

    private void internalAdd(MqMessageHolder mh) {
        messageQueue.add(mh);
    }

    private void internalRemove(MqMessageHolder mh) {
        messageQueue.remove(mh);
    }

    /**
     * 强制清空
     */
    @Override
    public void forceClear() {
        messageAddLock.lock();
        try {
            messageIndexer.set(0L);

            messageMap.clear();
            messageQueue.clear();

            //清空后 gc 下
            System.gc();
        } finally {
            messageAddLock.unlock();
        }
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
            if (msg.isDone() == false && (msg.getDistributeCount() >= times || msg.getDistributeCountPre() >= times)) {
                msgList.add(msg);
            }

            if (msgList.size() == count) {
                break;
            }
        }

        for (MqMessageHolder msg : msgList) {
            messageQueue.remove(msg);
            msg.delayed(System.currentTimeMillis());
            messageQueue.add(msg);
        }
    }

    /**
     * 执行转发
     */
    protected boolean transpond0(MqMessageHolder messageHolder) {
        if (messageHolder.isDone()) {
            //已完成
            internalDelete(messageHolder);
            return true;
        }

        if (messageHolder.getExpiration() > 0 && messageHolder.getExpiration() < System.currentTimeMillis()) {
            //已过期
            internalDelete(messageHolder);

            if (log.isWarnEnabled()) {
                log.warn("Queue: message have expired, key={}", messageHolder.getKey());
            }
            return true;
        }


        //获取一个会话（轮询负载均衡）
        Session s1 = null;

        //获取会话
        if (serviceListener.proxyMode) {
            s1 = serviceListener.brokerListener.getSessionAny();
        } else {
            s1 = serviceListener.brokerListener.getPlayerAny(messageHolder.getSender());
        }

        if (s1 != null) {
            try {
                //开始请求确认
                serviceListener.qpsDistribute.record();
                s1.sendAndRequest(MqConstants.MQ_EVENT_REQUEST, messageHolder.getEntity(), MqNextTime.maxConsumeMillis()).thenReply(r -> {
                    //进入正常队列
                    int ack = Integer.parseInt(r.metaOrDefault(MqConstants.MQ_META_ACK, "0"));
                    if (ack == 1) {
                        //提交
                        affirmAtDo(messageHolder, false);
                    } else {
                        //回滚
                        affirmAtDo(messageHolder, true);
                    }
                }).thenError(err -> {
                    //进入延后队列
                    internalAdd(messageHolder.delayed());

                    if (log.isDebugEnabled()) {
                        log.debug("Queue: request then error, key={}",
                                messageHolder.getKey(), err);
                    }
                });
            } catch (Throwable e) {
                //如果无效，则移掉
                if (s1 != null) {
                    if (s1.isValid() == false) {
                        sessionRemove(s1);
                    }
                }

                //进入延后队列
                internalAdd(messageHolder.delayed());

                if (log.isWarnEnabled()) {
                    log.warn("Queue: request error, key={}",
                            messageHolder.getKey(), e);
                }
            }
        } else {
            //::进入延后队列
            internalAdd(messageHolder.delayed());

            //记日志
            if (log.isDebugEnabled()) {
                if (serviceListener.proxyMode) {
                    log.debug("Queue: request: broker no sessions, times={}, key={}",
                            messageHolder.getDistributeCount(),
                            messageHolder.getKey());
                } else {
                    log.debug("Queue: request: @{} no sessions, times={}, key={}",
                            messageHolder.getSender(),
                            messageHolder.getDistributeCount(),
                            messageHolder.getKey());
                }
            }
        }

        return true;
    }

    /**
     * 执行派发
     */
    protected boolean distribute0(MqMessageHolder messageHolder) {
        if (messageHolder.isDone()) {
            //已完成
            internalDelete(messageHolder);
            return true;
        }

        if (messageHolder.getExpiration() > 0 && messageHolder.getExpiration() < System.currentTimeMillis()) {
            //已过期
            internalDelete(messageHolder);

            if (log.isWarnEnabled()) {
                log.warn("Queue: message have expired, key={}", messageHolder.getKey());
            }
            return true;
        }

        if (messageHolder.isSequence()) {
            if (messageHolder.getDistributeTimeRef() > System.currentTimeMillis()) {
                //如果未到，提前结束
                internalAdd(messageHolder);
                return false;
            }

            //如果是顺序消息
            sequenceLock.set(true);
        }

        //如果有会话
        if (sessionCount() > 0) {
            //获取一个会话（轮询负载均衡）
            Session s1 = sessionGetOne(messageHolder);

            //::派发
            try {
                if (s1 == null) {
                    //进入延后队列
                    internalAdd(messageHolder.delayed());
                    sequenceLock.set(false);
                } else {
                    distributeDo(s1, messageHolder);
                }
            } catch (Throwable e) {
                //如果无效，则移掉
                if (s1 != null && s1.isValid() == false) {
                    sessionRemove(s1);
                }

                //进入延后队列
                internalAdd(messageHolder.delayed());
                sequenceLock.set(false);

                //记日志
                if (log.isWarnEnabled()) {
                    log.warn("Queue: distribute error, key={}",
                            messageHolder.getKey(), e);
                }
            }
        } else {
            //::进入延后队列
            internalAdd(messageHolder.delayed());
            sequenceLock.set(false);

            //记日志
            if (log.isDebugEnabled()) {
                log.debug("Queue: distribute: @{} no sessions, times={}, key={}",
                        consumerGroup,
                        messageHolder.getDistributeCount(),
                        messageHolder.getKey());
            }
        }

        return true;
    }

    /**
     * 派发执行
     */
    private void distributeDo(Session s1, MqMessageHolder messageHolder) throws IOException {
        //观察者::派发时（在元信息调整之后，再观察）
        watcher.onDistribute(topic, consumerGroup, messageHolder);

        if (messageHolder.getQos() > 0 && messageHolder.isBroadcast() == false) {
            //::Qos1

            //1.给会话发送消息 //如果有异步，上面会加入队列
            serviceListener.qpsDistribute.record();
            s1.sendAndRequest(MqConstants.MQ_EVENT_DISTRIBUTE, messageHolder.getEntity(), MqNextTime.maxConsumeMillis()).thenReply(r -> {
                int ack = Integer.parseInt(r.metaOrDefault(MqConstants.MQ_META_ACK, "0"));
                acknowledgeDo(messageHolder, ack, true);
            }).thenError(err -> {
                acknowledgeDo(messageHolder, 0, true);
            });

            //2.添加保险延时任务：如果没有回执就重发 //重新入队列，是避免重启时数据丢失
            messageHolder.preDelayed(System.currentTimeMillis() + MqNextTime.maxConsumeMillis());
            internalAdd(messageHolder);
        } else {
            //::Qos0
            if (messageHolder.isBroadcast()) {
                for (Session s0 : sessionAll()) {
                    if (SessionUtils.isActive(s0)) {
                        serviceListener.qpsDistribute.record();
                        s0.send(MqConstants.MQ_EVENT_DISTRIBUTE, messageHolder.getEntity());
                    }
                }
            } else {
                serviceListener.qpsDistribute.record();
                s1.send(MqConstants.MQ_EVENT_DISTRIBUTE, messageHolder.getEntity());
            }

            acknowledgeDo(messageHolder, 1, false);
        }
    }

    @Override
    public void acknowledgeAt(String key, int ack) {
        MqMessageHolder messageHolder = messageMap.get(key);
        if (messageHolder != null) {
            acknowledgeDo(messageHolder, ack, true);
        }
    }

    private void acknowledgeDo(MqMessageHolder messageHolder, int ack, boolean removeQueue) {
        try {
            if (messageMap.containsKey(messageHolder.getKey()) == false) {
                //广播消息，会有多次回调；可能已结束了
                return;
            }

            if (ack > 0) {
                //ok
                internalDelete(messageHolder);
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

            //观察者::回执时
            watcher.onAcknowledge(topic, consumerGroup, messageHolder, ack > 0);
        } finally {
            sequenceLock.set(false);
        }
    }

    protected void internalDelete(MqMessageHolder messageHolder) {
        watcher.onRemove(topic, consumerGroup, messageHolder);
        messageMap.remove(messageHolder.getKey());
    }
}