package org.noear.folkmq.common;

import org.noear.folkmq.client.MqMessage;
import org.noear.socketd.transport.core.Entity;
import org.noear.socketd.transport.core.Message;
import org.noear.socketd.transport.core.entity.StringEntity;

/**
 * 消息元信息分析器
 *
 * @author noear
 * @since 1.1
 */
public interface MqResolver {
    String getTid(Message m);

    String getTopic(Message m);

    String getConsumerGroup(Message m);

    void setConsumerGroup(Entity m, String consumerGroup);

    int getQos(Message m);

    int getTimes(Message m);

    void setTimes(Entity m, int times);

    long getExpiration(Message m);

    String getPartition(Message m);

    long getScheduled(Message m);

    void setScheduled(Entity m, long scheduled);

    boolean isSequence(Message m);

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
