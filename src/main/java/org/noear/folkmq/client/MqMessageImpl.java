package org.noear.folkmq.client;


import org.noear.folkmq.MqConstants;
import org.noear.socketd.transport.core.Message;

import java.io.IOException;

/**
 * 消息结构体实现
 *
 * @author noear
 * @since 1.0
 */
public class MqMessageImpl implements MqMessage {
    private final transient MqClientInternal clientInternal;
    private final transient Message from;

    private final String tid;
    private final String topic;
    private final int times;
    private final String content;

    public MqMessageImpl(MqClientInternal clientInternal, Message from) {
        this.clientInternal = clientInternal;
        this.from = from;

        this.tid = from.metaOrDefault(MqConstants.MQ_TID, "");
        this.topic = from.metaOrDefault(MqConstants.MQ_TOPIC, "");
        this.times = Integer.parseInt(from.metaOrDefault(MqConstants.MQ_TIMES, "0"));
        this.content = from.dataAsString();
    }

    @Override
    public String getTid() {
        return tid;
    }

    @Override
    public String getTopic() {
        return topic;
    }

    @Override
    public int getTimes() {
        return times;
    }

    @Override
    public String getContent() {
        return content;
    }


    @Override
    public void acknowledge(boolean isOk) throws IOException {
        clientInternal.acknowledge(from, isOk);
    }

    @Override
    public String toString() {
        return "MqMessage{" +
                "id='" + tid + '\'' +
                ", topic='" + topic + '\'' +
                ", times=" + times +
                ", content='" + content + '\'' +
                '}';
    }
}
