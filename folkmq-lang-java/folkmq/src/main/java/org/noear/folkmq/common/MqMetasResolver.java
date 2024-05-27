package org.noear.folkmq.common;

import org.noear.folkmq.client.MqMessage;
import org.noear.socketd.transport.core.Entity;
import org.noear.socketd.transport.core.Message;
import org.noear.socketd.transport.core.entity.EntityDefault;

/**
 * 消息元信息分析器
 *
 * @author noear
 * @since 1.2
 */
public interface MqMetasResolver {
    /**
     * 版本号
     * */
    int version();

    /**
     * 获取发送人
     * */
    String getSender(Entity m);

    /**
     * 获取主键
     */
    String getKey(Entity m);

    /**
     * 获取标签
     * */
    String getTag(Entity m);

    /**
     * 获取主题
     */
    String getTopic(Entity m);

    /**
     * 获取消费者组
     */
    String getConsumerGroup(Entity m);

    /**
     * 设置消费者组
     */
    void setConsumerGroup(Entity m, String consumerGroup);

    /**
     * 获取质量等级（0或1）
     */
    int getQos(Entity m);

    /**
     * 获取派发次数
     */
    int getTimes(Entity m);

    /**
     * 设置派发次数
     */
    void setTimes(Entity m, int times);

    /**
     * 获取过期时间
     */
    long getExpiration(Entity m);

    /**
     * 设置过期时间
     * */
    void setExpiration(Entity m, Long expiration);

    /**
     * 备份过期时间
     *
     * @param isBak 是否为备份（true:备份，false:恢复）
     * */
    void bakExpiration(Entity m, boolean isBak);

    /**
     * 获取定时时间
     */
    long getScheduled(Entity m);

    /**
     * 设置定时时间
     */
    void setScheduled(Entity m, long scheduled);

    /**
     * 备份定时时间
     *
     * @param isBak 是否为备份（true:备份，false:恢复）
     */
    void bakScheduled(Entity m, boolean isBak);

    /**
     * 是否有序
     */
    boolean isSequence(Entity m);

    /**
     * 是否广播
     * */
    boolean isBroadcast(Entity m);

    /**
     * 是否事务
     */
    boolean isTransaction(Entity m);

    /**
     * 设置事务
     * */
    void setTransaction(Entity m, boolean isTransaction);

    /**
     * 发布实体构建
     *
     * @param topic   主题
     * @param message 消息
     */
    EntityDefault publishEntityBuild(String topic, MqMessage message);

    /**
     * 路由消息构建
     *
     * @param topic   主题
     * @param message 消息
     */
    Message routingMessageBuild(String topic, MqMessage message);
}
