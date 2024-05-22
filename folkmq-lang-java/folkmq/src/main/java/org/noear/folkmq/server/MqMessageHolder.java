package org.noear.folkmq.server;

import org.noear.folkmq.common.MqMetasResolver;
import org.noear.socketd.transport.core.entity.EntityDefault;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 消息持有人（为消息添加状态信息）
 *
 * @author noear
 * @since 1.0
 */
public class MqMessageHolder implements Delayed {
    private final MqData message;
    public final MqMetasResolver mr;
    //消息实体
    private final EntityDefault entity;

    //是否事务
    private boolean transaction;

    //派发时间
    private volatile long distributeTime;
    private volatile long distributeTimeRef;
    //派发次数
    private volatile int distributeCount;
    //派发顺序
    private volatile long distributeIdx;
    //是否完成
    private AtomicBoolean isDone;

    public MqMessageHolder(MqData mqMessage, String queueName, String consumerGroup) {
        this.message = mqMessage;
        this.mr = mqMessage.mr;
        this.entity = new EntityDefault().dataSet(message.source.data()).metaMapPut(message.source.metaMap());

        message.mr.setConsumerGroup(entity, consumerGroup);

        this.isDone = new AtomicBoolean();

        this.transaction = message.transaction;
        this.distributeCount = message.times;
        this.distributeTimeRef = message.scheduled;
        this.distributeTime = distributeTimeRef;

        if (message.sequence) {
            this.entity.at(queueName);
        } else {
            this.entity.at(queueName + "!");
        }

        if (transaction) {
            this.entity.at(message.sender);
        }
    }

    /**
     * 发送人
     */
    public String getSender() {
        return message.sender;
    }

    /**
     * 获取投放目标
     */
    public String getAtName() {
        return message.atName;
    }

    /**
     * 获取消息主键
     */
    public String getKey() {
        return message.key;
    }

    /**
     * 获取消息内容
     */
    public EntityDefault getEntity() {
        return entity;
    }

    /**
     * 质量等级（0 或 1）
     */
    public int getQos() {
        return message.qos;
    }

    /**
     * 过期时间
     */
    public long getExpiration() {
        return message.expiration;
    }

    /**
     * 是否事务
     */
    public boolean isTransaction() {
        return transaction;
    }

    public MqMessageHolder noTransaction() {
        transaction = false;
        distributeCount = 0;
        distributeTimeRef = System.currentTimeMillis();
        distributeTime = distributeTimeRef;

        //设置新的派发次数和下次时间
        message.mr.setTimes(entity, distributeCount);
        message.mr.setScheduled(entity, distributeTime);
        message.mr.setExpiration(entity, null);
        message.mr.setTransaction(entity, false);

        return this;
    }

    /**
     * 是否广播
     * */
    public boolean isBroadcast(){
        return message.broadcast;
    }

    /**
     * 是否顺序
     */
    public boolean isSequence() {
        return message.sequence;
    }

    /**
     * 获取顺序分片
     */
    public String getSequenceSharding() {
        return message.sequenceSharding;
    }

    /**
     * 设置派发时间
     */
    public void setDistributeTime(long distributeTime) {
        this.distributeTimeRef = distributeTime;

        if (isSequence() == false) {
            this.distributeTime = distributeTimeRef;
        }
    }

    /**
     * 设置派发顺序位
     */
    public void setDistributeIdx(long distributeIdx) {
        this.distributeIdx = distributeIdx;
    }

    /**
     * 获取派发时间（单位：毫秒）
     */
    public long getDistributeTime() {
        return distributeTime;
    }

    public long getDistributeTimeRef() {
        return distributeTimeRef;
    }

    /**
     * 获取派发次数
     */
    public int getDistributeCount() {
        return distributeCount;
    }


    public boolean isDone() {
        return isDone.get();
    }

    public void setDone(boolean done) {
        isDone.set(done);
    }

    /**
     * 延后（生成下次派发时间）
     */
    public MqMessageHolder delayed() {
        distributeCount++;
        distributeTimeRef = MqNextTime.getNextTime(this);

        //设置新的派发次数和下次时间
        message.mr.setTimes(entity, distributeCount);

        if (isSequence() == false) {
            //如果不是顺序消息，调整队列里的派发时间；否则走外部了的时间控制
            distributeTime = distributeTimeRef;
            message.mr.setScheduled(entity, distributeTimeRef);
        }

        return this;
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(distributeTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed o) {
        if (this == o) {
            return 0;
        }

        MqMessageHolder dst = ((MqMessageHolder) o);
        long diff = this.distributeTime - dst.distributeTime;

        if (diff == 0) {
            if (this.distributeIdx < dst.distributeIdx) {
                return -1;
            } else {
                return 1;
            }
        }

        if (diff < 0) {
            return -1;
        } else {
            return 1;
        }
    }

    //不要加 hashCode, equals 重写！
}