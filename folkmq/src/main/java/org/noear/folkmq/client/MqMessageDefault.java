package org.noear.folkmq.client;


import org.noear.folkmq.common.MqConstants;
import org.noear.socketd.transport.core.Message;

import java.io.IOException;

/**
 * 消息结构体默认实现
 *
 * @author noear
 * @since 1.0
 */
public class MqMessageDefault implements MqMessage {
    protected final transient MqClientInternal clientInternal;
    protected final transient Message from;

    private final String tid;
    private final String topic;
    private final String content;
    private final int qos;
    private final int times;

    public MqMessageDefault(MqClientInternal clientInternal, Message from) {
        this.clientInternal = clientInternal;
        this.from = from;

        this.tid = from.metaOrDefault(MqConstants.MQ_META_TID, "");
        this.topic = from.metaOrDefault(MqConstants.MQ_META_TOPIC, "");
        this.content = from.dataAsString();

        this.qos = Integer.parseInt(from.metaOrDefault(MqConstants.MQ_META_QOS, "1"));
        this.times = Integer.parseInt(from.metaOrDefault(MqConstants.MQ_META_TIMES, "0"));
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
        clientInternal.acknowledge(this, isOk);
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