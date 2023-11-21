package org.noear.folkmq.client;

/**
 * 客户端
 *
 * @author noear
 * @since 1.0
 */
public interface MqClient extends MqConsumer, MqProducer {
    /**
     * 是否自动 ack
     * */
    MqClient autoAck(boolean auto);
}
