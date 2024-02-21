package org.noear.folkmq.client;

import org.noear.folkmq.common.MqResolver;
import org.noear.folkmq.common.MqUtils;
import org.noear.socketd.transport.core.Message;
import org.noear.socketd.transport.core.Session;

import java.io.IOException;
import java.util.Date;

/**
 * 收到的消息
 *
 * @author noear
 * @since 1.0
 */
public class MqMessageReceivedImpl implements MqMessageReceived {
    private final transient MqClientInternal clientInternal;
    private final transient Message from;
    private final transient Session session;

    private final String tid;
    private final String topic;
    private final String consumerGroup;
    private final String content;
    private final Date expiration;
    private final boolean sequence;
    private final int qos;
    private final int times;

    public MqMessageReceivedImpl(MqClientInternal clientInternal, Session session, Message from) {
        this.clientInternal = clientInternal;
        this.session = session;
        this.from = from;

        this.content = from.dataAsString();

        MqResolver mr = MqUtils.getOf(from);

        this.tid = mr.getTid(from);
        this.topic = mr.getTopic(from);
        this.consumerGroup = mr.getConsumerGroup(from);

        this.qos = mr.getQos(from);
        this.times = mr.getTimes(from);
        this.sequence = mr.isSequence(from);

        long expirationL = mr.getExpiration(from);
        if (expirationL == 0) {
            this.expiration = null;
        } else {
            this.expiration = new Date(expirationL);
        }
    }

    /**
     * 事务ID
     */
    @Override
    public String getTid() {
        return tid;
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

    /**
     * 内容
     */
    @Override
    public String getContent() {
        return content;
    }

    /**
     * 质量等级（0 或 1）
     */
    @Override
    public int getQos() {
        return qos;
    }

    /**
     * 过期时间
     */
    @Override
    public Date getExpiration() {
        return expiration;
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

    /**
     * 回执
     */
    @Override
    public void acknowledge(boolean isOk) throws IOException {
        //发送“回执”，向服务端反馈消费情况
        clientInternal.acknowledge(session, from, this, isOk);
    }

    @Override
    public String toString() {
        return "MqMessageReceived{" +
                "tid='" + tid + '\'' +
                ", topic='" + topic + '\'' +
                ", consumerGroup='" + consumerGroup + '\'' +
                ", content='" + content + '\'' +
                ", qos=" + qos +
                ", times=" + times +
                '}';
    }
}
