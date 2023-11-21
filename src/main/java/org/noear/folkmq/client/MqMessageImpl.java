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
    private final MqClientInternal clientInternal;
    private final Message message;
    private final String content;
    private final int times;

    public MqMessageImpl(MqClientInternal clientInternal, Message message) {
        this.clientInternal = clientInternal;
        this.message = message;
        this.content = message.dataAsString();
        this.times = Integer.parseInt(message.metaOrDefault(MqConstants.MQ_TIMES, "0"));
    }

    @Override
    public String getKey() {
        return message.sid();
    }

    @Override
    public String getContent() {
        return content;
    }

    @Override
    public int getTimes() {
        return times;
    }

    @Override
    public void acknowledge(boolean isOk) throws IOException {
        clientInternal.acknowledge(message, isOk);
    }

    @Override
    public String toString() {
        return getContent();
    }
}
