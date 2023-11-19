package org.noear.folkmq.client;

/**
 * 消费者处理
 *
 * @author noear
 * @since 1.0
 */
public interface MqConsumerHandler {
    /**
     * 处理
     */
    void handle(String topic, String message);
}
