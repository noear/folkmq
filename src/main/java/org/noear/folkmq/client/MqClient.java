package org.noear.folkmq.client;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.CompletableFuture;

/**
 * 客户端
 *
 * @author noear
 * @since 1.0
 */
public interface MqClient {
    /**
     * 是否自动 ack
     */
    MqClient autoAck(boolean auto);

    /**
     * 订阅
     *
     * @param topic           主题
     * @param consumer        消费者（实例 ip 或 集群 name）
     * @param consumerHandler 消费处理
     */
    void subscribe(String topic, String consumer, MqConsumerHandler consumerHandler) throws IOException;

    /**
     * 发送
     *
     * @param topic   主题
     * @param message 消息
     */
    CompletableFuture<?> publish(String topic, String message) throws IOException;

    /**
     * 发送
     *
     * @param topic     主题
     * @param message   消息
     * @param scheduled 设置预定执行时间
     */
    CompletableFuture<?> publish(String topic, String message, Date scheduled) throws IOException;
}
