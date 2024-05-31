package org.noear.folkmq.server;

import org.noear.folkmq.common.MqMetasResolver;
import org.noear.socketd.transport.core.Message;
import org.noear.socketd.transport.core.entity.EntityDefault;
import org.noear.socketd.transport.core.entity.MessageBuilder;

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
    public final MqMetasResolver mr;
    //消息草稿
    private final MqDraft draft;
    //消息实体
    private final EntityDefault entity;

    //是否事务
    private boolean transaction;

    //派发时间
    private volatile long distributeTime;
    private volatile long distributeTimeRef;
    //派发次数
    private volatile int distributeCount;
    private volatile int distributeCountPre;
    //派发顺序
    private volatile long distributeIdx;
    //是否完成
    private AtomicBoolean isDone;

    public MqMessageHolder(MqDraft draft, String queueName, String consumerGroup) {
        this.draft = draft;
        this.mr = draft.mr;
        this.entity = new EntityDefault().dataSet(this.draft.source.data()).metaMapPut(this.draft.source.metaMap());

        this.mr.setConsumerGroup(entity, consumerGroup);

        this.isDone = new AtomicBoolean();

        this.transaction = this.draft.transaction;

        this.distributeCount = this.draft.times;
        this.distributeCountPre = this.distributeCount;

        this.distributeTimeRef = this.draft.scheduled;
        this.distributeTime = this.distributeTimeRef;

        if (this.draft.sequence) {
            this.entity.at(queueName + "!");
        } else {
            this.entity.at(queueName);
        }

        if (transaction) {
            this.entity.at(this.draft.sender);
        }
    }

    /**
     * 发送人
     */
    public String getSender() {
        return draft.sender;
    }

    /**
     * 获取投放目标
     */
    public String getAtName() {
        return draft.atName;
    }

    /**
     * 获取消息主键
     */
    public String getKey() {
        return draft.key;
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
        return draft.qos;
    }

    /**
     * 过期时间
     */
    public long getExpiration() {
        return draft.expiration;
    }

    /**
     * 是否事务
     */
    public boolean isTransaction() {
        return transaction;
    }

    public Message noTransaction() {
        transaction = false;
        distributeCountPre = 0;
        distributeCount = 0;
        distributeTimeRef = System.currentTimeMillis();
        distributeTime = distributeTimeRef;

        //设置新的派发次数和下次时间
        mr.setTimes(entity, 0); //重新计数
        mr.bakScheduled(entity, false); //恢复定时
        mr.bakExpiration(entity, false); //恢复有效期
        mr.setTransaction(entity, false); //转为非事务

        return new MessageBuilder()
                .sid(getKey())
                .entity(getEntity()).build();
    }

    /**
     * 是否广播
     * */
    public boolean isBroadcast(){
        return draft.broadcast;
    }

    /**
     * 是否顺序
     */
    public boolean isSequence() {
        return draft.sequence;
    }

    /**
     * 获取顺序分片
     */
    public String getSequenceSharding() {
        return draft.sequenceSharding;
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

    public int getDistributeCountPre() {
        return distributeCountPre;
    }

    public boolean isDone() {
        return isDone.get();
    }

    public void setDone(boolean done) {
        isDone.set(done);
    }

    /**
     * 预延后
     * */
    public MqMessageHolder preDelayed(){
        distributeCountPre++;
        return this;
    }

    /**
     * 延后（生成下次派发时间）
     */
    public MqMessageHolder delayed() {
        distributeCount++;
        distributeTimeRef = MqNextTime.getNextTime(this);

        //设置新的派发次数和下次时间
        mr.setTimes(entity, distributeCount);

        if (isSequence() == false) {
            //如果不是顺序消息，调整队列里的派发时间；否则走外部了的时间控制
            distributeTime = distributeTimeRef;
            mr.setScheduled(entity, distributeTimeRef);
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