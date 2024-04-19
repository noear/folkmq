package org.noear.folkmq.client;

import org.noear.socketd.transport.client.ClientConfigHandler;
import org.noear.socketd.transport.stream.RequestStream;
import org.noear.socketd.utils.StrUtils;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * 消息客户端
 *
 * @author noear
 * @since 1.0
 * @since 1.2
 */
public interface MqClient {
    /**
     * 名字（即，默认消费者组）
     */
    String name();

    /**
     * 名字取为（即，默认消费者组）
     */
    MqClient nameAs(String name);

    /**
     * 命名空间
     */
    String namespace();

    /**
     * 命名空间
     */
    MqClient namespaceAs(String namespace);

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
     * 消费执行器
     */
    MqClient consumeExecutor(ExecutorService consumeExecutor);

    /**
     * 自动回执
     *
     * @param auto 自动（默认为 true）
     */
    MqClient autoAcknowledge(boolean auto);

    /**
     * 自动回执
     */
    boolean autoAcknowledge();

    /**
     * 接口调用
     */
    CompletableFuture<String> call(String apiName, String apiToken, String topic, String consumerGroup) throws IOException;

    /**
     * 订阅主题
     *
     * @param topic           主题
     * @param consumerGroup   消费者组
     * @param autoAck         是否自动回执
     * @param consumerHandler 消费处理
     */
    void subscribe(String topic, String consumerGroup, boolean autoAck, MqConsumeHandler consumerHandler) throws IOException;


    /**
     * 订阅主题
     *
     * @param topic           主题
     * @param consumerGroup   消费者组
     * @param consumerHandler 消费处理
     */
    default void subscribe(String topic, String consumerGroup, MqConsumeHandler consumerHandler) throws IOException {
        subscribe(topic, consumerGroup, autoAcknowledge(), consumerHandler);
    }


    /**
     * 订阅主题
     *
     * @param topic           主题
     * @param consumerHandler 消费处理
     */
    default void subscribe(String topic, MqConsumeHandler consumerHandler) throws IOException {
        //检查必要条件
        if (StrUtils.isEmpty(name())) {
            throw new IllegalArgumentException("Client 'name' can't be empty");
        }

        subscribe(topic, name(), autoAcknowledge(), consumerHandler);
    }

    /**
     * 取消订阅主题
     *
     * @param topic         主题
     * @param consumerGroup 消费者组
     */
    void unsubscribe(String topic, String consumerGroup) throws IOException;

    /**
     * 取消订阅主题
     *
     * @param topic 主题
     */
    default void unsubscribe(String topic) throws IOException {
        //检查必要条件
        if (StrUtils.isEmpty(name())) {
            throw new IllegalArgumentException("Client 'name' can't be empty");
        }

        unsubscribe(topic, name());
    }

    /**
     * 同步发布消息
     *
     * @param topic   主题
     * @param message 消息
     */
    void publish(String topic, MqMessage message) throws IOException;

    /**
     * 异步发布消息
     *
     * @param topic   主题
     * @param message 消息
     */
    CompletableFuture<Boolean> publishAsync(String topic, MqMessage message) throws IOException;

    /**
     * 取消发布
     *
     * @param topic 主题
     * @param key   消息主键
     */
    void unpublish(String topic, String key) throws IOException;

    /**
     * 取消发布
     *
     * @param topic 主题
     * @param key   消息主键
     */
    CompletableFuture<Boolean> unpublishAsync(String topic, String key) throws IOException;


    /**
     * 监听
     *
     * @param listenHandler 监听处理
     */
    void listen(MqConsumeHandler listenHandler);

    /**
     * 发送
     *
     * @param message 消息
     * @param toName  发送目标名字
     * @param timeout 超时（单位毫秒）
     */
    RequestStream send(MqMessage message, String toName, long timeout) throws IOException;

    /**
     * 发送
     *
     * @param message 消息
     * @param toName  发送目标名字
     */
    default RequestStream send(MqMessage message, String toName) throws IOException {
        return send(message, toName, 0L);
    }

    /**
     * 事务回查
     *
     * @param transactionCheckback 事务回查处理
     */
    MqClient transactionCheckback(MqTransactionCheckback transactionCheckback);

    /**
     * 新建事务
     */
    MqTransaction newTransaction();
}