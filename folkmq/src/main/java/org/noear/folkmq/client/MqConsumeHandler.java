package org.noear.folkmq.client;

/**
 * 消费处理器
 *
 * @author noear
 * @since 1.0
 */
@FunctionalInterface
public interface MqConsumeHandler {
    /**
     * 消费
     *
     * @param message 收到的消息
     */
    void consume(MqMessageReceived message) throws Exception;
}
