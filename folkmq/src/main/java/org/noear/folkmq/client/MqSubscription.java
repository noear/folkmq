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
    private final String consumer;
    private final MqConsumeHandler consumerHandler;


    /**
     * 主题
     */
    public String getTopic() {
        return topic;
    }

    /**
     * 消费者（实例 IP，或集群 Name）
     */
    public String getConsumer() {
        return consumer;
    }

    /**
     * 消费处理器
     */
    public MqConsumeHandler getConsumerHandler() {
        return consumerHandler;
    }

    /**
     * @param topic           主题
     * @param consumer        消费者（实例 ip 或 集群 name）
     * @param consumerHandler 消费处理器
     */
    public MqSubscription(String topic, String consumer, MqConsumeHandler consumerHandler) {
        this.topic = topic;
        this.consumer = consumer;
        this.consumerHandler = consumerHandler;
    }

    @Override
    public void consume(MqMessageReceived message) throws IOException {
        consumerHandler.consume(message);
    }
}
