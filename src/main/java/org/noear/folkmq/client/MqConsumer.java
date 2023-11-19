package org.noear.folkmq.client;

import java.io.IOException;

/**
 * 消费者
 *
 * @author noear
 * @since 1.0
 */
public interface MqConsumer {
    /**
     * 订阅
     *
     * @param topic   主题
     * @param handler 消费处理
     */
    void subscribe(String topic, MqConsumerHandler handler) throws IOException;
}
