package org.noear.folkmq.client;

import org.noear.socketd.transport.client.ClientConfigHandler;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.CompletableFuture;

/**
 * 消息客户端
 *
 * @author noear
 * @since 1.0
 */
public interface MqClient {
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
     */
    MqClient autoAcknowledge(boolean auto);

    /**
     * 订阅主题
     *
     * @param topic           主题
     * @param consumer        消费者（实例 ip 或 集群 name）
     * @param consumerHandler 消费处理
     */
    void subscribe(String topic, String consumer, MqConsumeHandler consumerHandler) throws IOException;

    /**
     * 取消订阅主题
     *
     * @param topic    主题
     * @param consumer 消费者（实例 ip 或 集群 name）
     */
    void unsubscribe(String topic, String consumer) throws IOException;

    /**
     * 发布消息
     *
     * @param topic   主题
     * @param content 消息内容
     */
    default CompletableFuture<?> publish(String topic, String content) throws IOException {
        return publish(topic, content, null, 1);
    }

    /**
     * 发布消息
     *
     * @param topic   主题
     * @param content 消息内容
     * @param qos     质量等级（0 或 1）
     */
    default CompletableFuture<?> publish(String topic, String content, int qos) throws IOException {
        return publish(topic, content, null, qos);
    }

    /**
     * 发布消息
     *
     * @param topic     主题
     * @param content   消息内容
     * @param scheduled 预定派发时间
     */
    default CompletableFuture<?> publish(String topic, String content, Date scheduled) throws IOException {
        return publish(topic, content, scheduled, 1);
    }

    /**
     * 发布消息
     *
     * @param topic     主题
     * @param content   消息内容
     * @param scheduled 预定派发时间
     * @param qos       质量等级（0 或 1）
     */
    CompletableFuture<?> publish(String topic, String content, Date scheduled, int qos) throws IOException;
}
