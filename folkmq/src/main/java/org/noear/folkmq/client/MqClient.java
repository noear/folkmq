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
 */
public interface MqClient extends Closeable {
    /**
     * 名字
     */
    String name();

    /**
     * 名字取为
     */
    MqClient nameAs(String name);

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
     * 消息处理执行器
     */
    MqClient handleExecutor(ExecutorService handleExecutor);

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


    /**
     * 请求
     */
    RequestStream request(String atName, String topic, MqMessage message) throws IOException;

    /**
     * 响应
     */
    MqClient response(MqResponder responder);

    /**
     * 创建事务
     */
    MqTransaction newTransaction();
}
