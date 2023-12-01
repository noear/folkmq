package org.noear.folkmq.server;

import org.noear.socketd.transport.core.Message;
import org.noear.socketd.transport.core.Session;

import java.util.Map;

/**
 * 主题消费者队列（服务端给 [一个主题+一个消费者] 安排一个队列，一个消费者可多个会话，只随机给一个会话派发）
 *
 * @author noear
 * @since 1.0
 */
public interface MqTopicConsumerQueue {
    /**
     * 获取主题
     */
    String getTopic();

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
     * 会话数量
     * */
    int sessionCount();

    /**
     * 添加消息
     */
    void add(MqMessageHolder messageHolder);

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