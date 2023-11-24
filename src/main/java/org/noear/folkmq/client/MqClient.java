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
     * 自动回执
     */
    MqClient autoAcknowledge(boolean auto);

    /**
     * 订阅主题
     *
     * @param topic           主题
     * @param consumer        消费者（实例 ip 或 集群 name）
     * @param consumerHandler 消费处理
     */
    void subscribe(String topic, String consumer, MqConsumerHandler consumerHandler) throws IOException;

    /**
     * 发布消息
     *
     * @param topic   主题
     * @param content 消息内容
     */
    CompletableFuture<?> publish(String topic, String content) throws IOException;

    /**
     * 发布消息
     *
     * @param topic     主题
     * @param content   消息内容
     * @param scheduled 预定派发时间
     */
    CompletableFuture<?> publish(String topic, String content, Date scheduled) throws IOException;
}
