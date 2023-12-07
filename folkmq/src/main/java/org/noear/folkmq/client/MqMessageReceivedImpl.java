package org.noear.folkmq.client;

import org.noear.folkmq.common.MqConstants;
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
    private final String content;
    private final Date scheduled;
    private final int qos;
    private final int times;

    public MqMessageReceivedImpl(MqClientInternal clientInternal, Session session, Message from) {
        this.clientInternal = clientInternal;
        this.session = session;
        this.from = from;

        this.tid = from.metaOrDefault(MqConstants.MQ_META_TID, "");
        this.topic = from.metaOrDefault(MqConstants.MQ_META_TOPIC, "");
        this.content = from.dataAsString();

        this.qos = Integer.parseInt(from.metaOrDefault(MqConstants.MQ_META_QOS, "1"));
        this.times = Integer.parseInt(from.metaOrDefault(MqConstants.MQ_META_TIMES, "0"));
        this.scheduled = null;
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
     * 内容
     */
    @Override
    public String getContent() {
        return content;
    }

    /**
     * 定时时间
     */
    @Override
    public Date getScheduled() {
        return scheduled;
    }

    /**
     * 质量等级（0 或 1）
     */
    @Override
    public int getQos() {
        return qos;
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
        return "MqMessage{" +
                "tid='" + tid + '\'' +
                ", topic='" + topic + '\'' +
                ", content='" + content + '\'' +
                ", qos=" + qos +
                ", times=" + times +
                '}';
    }
}
