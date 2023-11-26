package org.noear.folkmq.client;

import java.io.IOException;

/**
 * 消费者处理
 *
 * @author noear
 * @since 1.0
 */
public interface MqConsumerHandler {
    /**
     * 处理
     *
     * @param message 消息
     */
    void handle(MqMessage message) throws IOException;
}
