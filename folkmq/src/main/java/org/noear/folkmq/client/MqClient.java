package org.noear.folkmq.client;

import org.noear.socketd.transport.client.ClientConfigHandler;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/**
 * 消息客户端
 *
 * @author noear
 * @since 1.0
 */
public interface MqClient extends Closeable {
    /**
     * 连接
     */
    MqClient connect() throws IOException;

    /**
     * 断开连接
     */
    void disconnect() throws IOException;

    /**
     * 客户端配置
     */
    MqClient config(ClientConfigHandler configHandler);

    /**
     * 自动回执
     *
     * @param auto 自动（默认为 true）
     */
    MqClient autoAcknowledge(boolean auto);

    /**
     * 订阅主题
     *
     * @param topic           主题
     * @param consumerGroup   消费者组
     * @param consumerHandler 消费处理
     */
    void subscribe(String topic, String consumerGroup, MqConsumeHandler consumerHandler) throws IOException;

    /**
     * 取消订阅主题
     *
     * @param topic         主题
     * @param consumerGroup 消费者组
     */
    void unsubscribe(String topic, String consumerGroup) throws IOException;

    /**
     * 同步发布消息
     *
     * @param topic   主题
     * @param message 消息
     */
    void publish(String topic, IMqMessage message) throws IOException;

    /**
     * 异步发布消息
     *
     * @param topic   主题
     * @param message 消息
     */
    CompletableFuture<Boolean> publishAsync(String topic, IMqMessage message) throws IOException;

    /**
     * 取消发布
     *
     * @param topic 主题
     * @param tid   事务id
     */
    void unpublish(String topic, String tid) throws IOException;

    /**
     * 取消发布
     *
     * @param topic 主题
     * @param tid   事务id
     */
    CompletableFuture<Boolean> unpublishAsync(String topic, String tid) throws IOException;
}
