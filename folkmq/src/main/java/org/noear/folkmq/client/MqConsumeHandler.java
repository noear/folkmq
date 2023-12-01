package org.noear.folkmq.client;

import java.io.IOException;

/**
 * 消费处理器
 *
 * @author noear
 * @since 1.0
 */
public interface MqConsumeHandler {
    /**
     * 消费
     *
     * @param message 派发的消息
     */
    void consume(MqMessage message) throws IOException;
}
