package org.noear.folkmq.client;

import org.noear.folkmq.common.MqConstants;
import org.noear.folkmq.common.MqMetasResolver;
import org.noear.folkmq.common.MqUtils;
import org.noear.socketd.transport.core.Entity;
import org.noear.socketd.transport.core.Message;
import org.noear.socketd.transport.core.Session;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

/**
 * 收到的消息
 *
 * @author noear
 * @since 1.0
 */
public class MqMessageReceivedImpl implements MqMessageReceived {
    private final transient MqClientInternal clientInternal;
    private final transient Message source;
    private final transient Session session;

    private final String sender;
    private final String tid;
    private final String tag;
    private final String topic;
    private final String consumerGroup;
    private final Date expiration;
    private final boolean sequence;
    private final boolean transaction;
    private final int qos;
    private final int times;

    public MqMessageReceivedImpl(MqClientInternal clientInternal, Session session, Message source) {
        this.clientInternal = clientInternal;
        this.session = session;
        this.source = source;

        MqMetasResolver mr = MqUtils.getOf(source);

        this.sender = mr.getSender(source);

        this.tid = mr.getTid(source);
        this.tag = mr.getTag(source);
        this.topic = mr.getTopic(source);
        this.consumerGroup = mr.getConsumerGroup(source);

        this.qos = mr.getQos(source);
        this.times = mr.getTimes(source);
        this.sequence = mr.isSequence(source);
        this.transaction = mr.isTransaction(source);

        long expirationL = mr.getExpiration(source);
        if (expirationL == 0) {
            this.expiration = null;
        } else {
            this.expiration = new Date(expirationL);
        }
    }

    /**
     * 获取消息源
     */
    public Message getSource() {
        return source;
    }

    /**
     * 发送人
     */
    @Override
    public String getSender() {
        return sender;
    }

    /**
     * 跟踪ID
     */
    @Override
    public String getTid() {
        return tid;
    }

    /**
     * 标签
     */
    @Override
    public String getTag() {
        return tag;
    }

    /**
     * 主题
     */
    @Override
    public String getTopic() {
        return topic;
    }

    /**
     * 消费者组
     */
    public String getConsumerGroup() {
        return consumerGroup;
    }

    @Override
    public byte[] getBody() {
        return source.dataAsBytes();
    }

    @Override
    public String getBodyAsString() {
        return source.dataAsString();
    }

    /**
     * 质量等级（0 或 1）
     */
    @Override
    public int getQos() {
        return qos;
    }

    @Override
    public String getAttr(String name) {
        return source.meta(MqConstants.MQ_ATTR_PREFIX + name);
    }


    /**
     * 过期时间
     */
    @Override
    public Date getExpiration() {
        return expiration;
    }

    /**
     * 是否事务
     */
    @Override
    public boolean isTransaction() {
        return transaction;
    }

    /**
     * 是否有序
     */
    @Override
    public boolean isSequence() {
        return sequence;
    }

    /**
     * 已派发次数
     */
    @Override
    public int getTimes() {
        return times;
    }


    @Deprecated
    @Override
    public void acknowledge(boolean isOk, Entity reply) throws IOException {
        //发送“回执”，向服务端反馈消费情况
        clientInternal.reply(session, source, this, isOk, reply);
    }

    @Override
    public void acknowledge(boolean isOk) throws IOException {
        clientInternal.reply(session, source, this, isOk, null);
    }

    @Override
    public void response(Entity entity) throws IOException {
        clientInternal.reply(session, source, this, true, entity);
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("MqMessageReceived{");
        buf.append("tid='").append(tid).append("',");
        buf.append("tag='").append(tag).append("',");
        buf.append("topic='").append(topic).append("',");
        buf.append("body='").append(getBodyAsString()).append("',");

        for (Map.Entry<String, String> kv : source.metaMap().entrySet()) {
            if (kv.getKey().startsWith(MqConstants.MQ_ATTR_PREFIX)) {
                buf.append(kv.getKey()).append("='").append(kv.getValue()).append("',");
            }
        }

        buf.setLength(buf.length() - 1);
        buf.append("}");

        return buf.toString();
    }
}