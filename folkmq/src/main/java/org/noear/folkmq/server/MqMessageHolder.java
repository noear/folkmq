package org.noear.folkmq.server;

import org.noear.folkmq.common.MqResolver;
import org.noear.socketd.transport.core.Message;
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
    private final MqResolver mr;
    //消息内容
    private final EntityDefault content;
    //事务Id
    private final String tid;
    //投放目标
    private final String atName;
    //质量等级（0 或 1）
    private final int qos;
    //存活时间
    private final long expiration;
    //哈希分区
    private final String partition;
    //是否有序
    private final boolean sequence;

    //派发时间
    private volatile long distributeTime;
    //派发次数
    private volatile int distributeCount;
    //派发顺序
    private volatile long distributeIdx;
    //是否完成
    private AtomicBoolean isDone;

    public MqMessageHolder(MqResolver mr, String queueName, String consumerGroup, Message from, String tid, int qos, boolean sequence, long expiration, String partition, int distributeCount, long distributeTime) {
        this.mr = mr;
        this.atName = from.atName();
        this.content = new EntityDefault().dataSet(from.data()).metaMapPut(from.metaMap());

        mr.setConsumerGroup(content, consumerGroup);

        if (sequence) {
            this.content.at(queueName);
        } else {
            this.content.at(queueName + "!");
        }

        this.isDone = new AtomicBoolean();

        this.tid = tid;
        this.qos = qos;
        this.expiration = expiration;
        this.partition = partition;
        this.sequence = sequence;
        this.distributeCount = distributeCount;
        this.distributeTime = distributeTime;
    }

    /**
     * 获取事务Id
     */
    public String getTid() {
        return tid;
    }

    /**
     * 获取投放目标
     */
    public String getAtName() {
        return atName;
    }

    /**
     * 获取消息内容
     */
    public EntityDefault getContent() {
        return content;
    }

    /**
     * 质量等级（0 或 1）
     */
    public int getQos() {
        return qos;
    }

    /**
     * 过期时间
     */
    public long getExpiration() {
        return expiration;
    }

    /**
     * 哈希分区
     */
    public String getPartition() {
        return partition;
    }

    /**
     * 是否有序
     */
    public boolean isSequence() {
        return sequence;
    }

    /**
     * 设置派发时间
     */
    public void setDistributeTime(long distributeTime) {
        this.distributeTime = distributeTime;
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
        distributeTime = MqNextTime.getNextTime(this);

        //设置新的派发次数和下次时间
        mr.setTimes(content, distributeCount);
        mr.setScheduled(content, distributeTime);

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