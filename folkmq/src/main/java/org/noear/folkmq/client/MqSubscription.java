package org.noear.folkmq.client;

import java.io.IOException;

/**
 * 订阅记录
 *
 * @author noear
 * @since 1.0
 */
public class MqSubscription implements MqConsumeHandler {
    private final String topic;
    private final String consumerGroup;
    private final MqConsumeHandler consumerHandler;


    /**
     * 主题
     */
    public String getTopic() {
        return topic;
    }

    /**
     * 消费者组
     */
    public String getConsumerGroup() {
        return consumerGroup;
    }

    /**
     * 消费处理器
     */
    public MqConsumeHandler getConsumerHandler() {
        return consumerHandler;
    }

    /**
     * @param topic           主题
     * @param consumerGroup   消费者组
     * @param consumerHandler 消费处理器
     */
    public MqSubscription(String topic, String consumerGroup, MqConsumeHandler consumerHandler) {
        this.topic = topic;
        this.consumerGroup = consumerGroup;
        this.consumerHandler = consumerHandler;
    }

    /**
     * 消费
     *
     * @param message 收到的消息
     */
    @Override
    public void consume(MqMessageReceived message) throws IOException {
        consumerHandler.consume(message);
    }
}
