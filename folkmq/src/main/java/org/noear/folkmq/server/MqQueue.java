package org.noear.folkmq.server;

import org.noear.socketd.transport.core.Session;

/**
 * 队列（服务端给每对 [主题#消费者组] 安排一个队列，队列内通过轮询负载平衡派发）
 *
 * @author noear
 * @since 1.0
 */
public interface MqQueue {
    /**
     * 获取主题
     */
    String getTopic();

    /**
     * 获取消费组
     */
    String getConsumerGroup();

    /**
     * 获取队列名
     * */
    String getQueueName();

    /**
     * 添加消费者会话
     */
    void addSession(Session session);

    /**
     * 移除消费者会话
     */
    void removeSession(Session session);

    /**
     * 消费者会话数量
     * */
    int sessionCount();

    /**
     * 添加消息
     */
    void add(MqMessageHolder messageHolder);

    /**
     * 派发消息
     * */
    boolean distribute();

    /**
     * 消息总量
     * */
    int messageTotal();

    /**
     * 消息总量2（用作校验）
     * */
    int messageTotal2();

    /**
     * 关闭
     */
    void close();
}