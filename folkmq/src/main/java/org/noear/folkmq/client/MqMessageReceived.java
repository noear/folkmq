package org.noear.folkmq.client;

import org.noear.socketd.transport.core.Entity;

import java.io.IOException;

/**
 * 收到的消息接口
 *
 * @author noear
 * @since 1.0
 */
public interface MqMessageReceived extends IMqMessage {
    /**
     * 主题
     */
    String getTopic();

    /**
     * 消费者组
     */
    String getConsumerGroup();

    /**
     * 已派发次数
     */
    int getTimes();


    /**
     * 回执
     */
    default void acknowledge(boolean isOk) throws IOException {
        acknowledge(isOk, null);
    }

    /**
     * 回执
     */
    void acknowledge(boolean isOk, Entity reply) throws IOException;
}
