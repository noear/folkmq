package org.noear.folkmq.client;

import org.noear.folkmq.common.MqConstants;

/**
 * 订阅记录
 *
 * @author noear
 * @since 1.0
 */
public class MqSubscription implements MqConsumeHandler {
    private final String topic;
    private final String consumerGroup;
    private final String queueName;
    private final boolean autoAck;
    private final MqConsumeHandler consumeHandler;


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
     * 是否自动回执
     */
    public boolean isAutoAck() {
        return autoAck;
    }

    /**
     * 相关队列名
     */
    public String getQueueName() {
        return queueName;
    }

    /**
     * 消费处理器
     */
    public MqConsumeHandler getConsumeHandler() {
        return consumeHandler;
    }

    /**
     * @param topic          主题
     * @param consumerGroup  消费者组
     * @param consumeHandler 消费处理器
     */
    public MqSubscription(String topic, String consumerGroup, boolean autoAck, MqConsumeHandler consumeHandler) {
        this.topic = topic;
        this.consumerGroup = consumerGroup;
        this.autoAck = autoAck;
        this.queueName = topic + MqConstants.SEPARATOR_TOPIC_CONSUMER_GROUP + consumerGroup;
        this.consumeHandler = consumeHandler;
    }

    /**
     * 消费
     *
     * @param message 收到的消息
     */
    @Override
    public void consume(MqMessageReceived message) throws Exception {
        consumeHandler.consume(message);
    }
}
