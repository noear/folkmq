package org.noear.folkmq.server;

import org.noear.socketd.transport.core.Session;

/**
 * 消费者队列（一个消费者一个队列，一个消费者可多个会话）
 *
 * @author noear
 * @since 1.0
 */
public interface MqConsumerQueue {
    /**
     * 获取消费者
     */
    String getConsumer();

    /**
     * 添加消费者会话
     */
    void addSession(Session session);

    /**
     * 移除消费者会话
     */
    void removeSession(Session session);

    /**
     * 推入消息
     */
    void push(MqMessageHolder messageHolder);
}