package org.noear.folkmq.common;

import org.noear.folkmq.client.MqMessage;
import org.noear.socketd.transport.core.Entity;
import org.noear.socketd.transport.core.Message;
import org.noear.socketd.transport.core.entity.StringEntity;

/**
 * 消息元信息分析器
 *
 * @author noear
 * @since 1.2
 */
public interface MqResolver {
    /**
     * 获取事务id
     */
    String getTid(Message m);

    /**
     * 获取主题
     */
    String getTopic(Message m);

    /**
     * 获取消费者组
     */
    String getConsumerGroup(Message m);

    /**
     * 设置消费者组
     */
    void setConsumerGroup(Entity m, String consumerGroup);

    /**
     * 获取质量等级（0或1）
     */
    int getQos(Message m);

    /**
     * 获取派发次数
     */
    int getTimes(Message m);

    /**
     * 设置派发次数
     */
    void setTimes(Entity m, int times);

    /**
     * 获取过期时间
     */
    long getExpiration(Message m);

    /**
     * 获取定时时间
     */
    long getScheduled(Message m);

    /**
     * 设置定时时间
     */
    void setScheduled(Entity m, long scheduled);

    /**
     * 是否为有序
     */
    boolean isSequence(Message m);

    /**
     * 是否为事务
     */
    boolean isTransaction(Message m);

    /**
     * 发布实体构建
     *
     * @param topic   主题
     * @param message 消息
     */
    StringEntity publishEntityBuild(String topic, MqMessage message);

    /**
     * 路由消息构建
     *
     * @param topic   主题
     * @param message 消息
     */
    Message routingMessageBuild(String topic, MqMessage message);
}
