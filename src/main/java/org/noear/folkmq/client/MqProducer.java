package org.noear.folkmq.client;

import java.io.IOException;

/**
 * 生产者
 *
 * @author noear
 * @since 1.0
 */
public interface MqProducer {
    /**
     * 发送
     *
     * @param topic   主题
     * @param message 消息
     */
    void publish(String topic, String message) throws IOException;
}
