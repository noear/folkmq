package org.noear.folkmq.server;

import org.noear.folkmq.common.MqConstants;
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
    //消息内容
    private final EntityDefault content;
    //事务Id
    private final String tid;
    //质量等级（0 或 1）
    private final int qos;

    //派发时间
    private volatile long distributeTime;
    //派发次数
    private volatile int distributeCount;
    //是否完成
    private AtomicBoolean isDone;

    public MqMessageHolder(String queueName, String consumerGroup, Message from, String tid, int qos, int distributeCount, long distributeTime) {
        this.content = new EntityDefault().data(from.data()).metaMap(from.metaMap());
        this.content.meta(MqConstants.MQ_META_CONSUMER_GROUP, consumerGroup).at(queueName);

        this.isDone = new AtomicBoolean();

        this.tid = tid;
        this.qos = qos;
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

    public void setDistributeTime(long distributeTime) {
        this.distributeTime = distributeTime;
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
        content.meta(MqConstants.MQ_META_TIMES, String.valueOf(distributeCount));
        content.meta(MqConstants.MQ_META_SCHEDULED, String.valueOf(distributeTime));

        return this;
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(distributeTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed o) {
        long f = this.distributeTime - ((MqMessageHolder) o).distributeTime;
        return (int) f;
    }

    //不要加 hashCode, equals 重写！
}
