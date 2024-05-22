package org.noear.folkmq.server;

import org.noear.folkmq.common.MqMetasResolver;
import org.noear.socketd.transport.core.EntityMetas;
import org.noear.socketd.transport.core.Message;

/**
 * @author noear
 * @since 1.4
 */
public class MqData {
    public final MqMetasResolver mr;
    public final Message source;
    public final String sender;
    public final String atName;
    public final String key;
    public final String topic;
    public final int qos;
    public final int times;
    public final long expiration;
    public final long scheduled;
    public final boolean sequence;
    public final boolean broadcast;
    public final boolean transaction;
    public final String sequenceSharding;

    public MqData(MqMetasResolver mr, Message source) {
        this.mr = mr;
        this.source = source;

        //复用解析
        sender = mr.getSender(source);
        atName = source.atName();

        key = mr.getKey(source);
        topic = mr.getTopic(source);
        qos = mr.getQos(source);
        times = mr.getTimes(source);
        expiration = mr.getExpiration(source);

        sequence = mr.isSequence(source);
        sequenceSharding = source.meta(EntityMetas.META_X_HASH);


        broadcast = mr.isBroadcast(source);
        transaction = mr.isTransaction(source);

        long scheduledTmp = mr.getScheduled(source);

        if (scheduledTmp == 0) {
            //默认为当前ms（相对于后面者，有个排序作用）
            scheduled = System.currentTimeMillis();
        } else {
            scheduled = scheduledTmp;
        }
    }
}
