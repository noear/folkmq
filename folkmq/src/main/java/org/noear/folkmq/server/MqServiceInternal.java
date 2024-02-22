package org.noear.folkmq.server;

import org.noear.folkmq.common.MqResolver;
import org.noear.socketd.transport.core.Message;
import org.noear.socketd.transport.core.Session;
import org.noear.socketd.transport.core.listener.MessageHandler;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * 消息服务内部接口
 *
 * @author noear
 * @since 1.0
 */
public interface MqServiceInternal {
    /**
     * 获取所有会话
     */
    Collection<Session> getSessionAll();

    /**
     * 获取所有会话数量
     */
    int getSessionCount();

    /**
     * 获取订阅关系(topic=>[queueName]) //queueName='topic#consumer'
     */
    Map<String, Set<String>> getSubscribeMap();

    /**
     * 获取队列字典(queueName=>Queue)
     */
    Map<String, MqQueue> getQueueMap();

    /**
     * 获取队列
     */
    MqQueue getQueue(String queueName);

    /**
     * 移除队列
     */
    void removeQueue(String queueName);

    /**
     * 执行订阅
     *
     * @param topic         主题
     * @param consumerGroup 消费者组
     * @param session       会话（即消费者）
     */
    void subscribeDo(String topic, String consumerGroup, Session session);

    /**
     * 执行取消订阅
     *
     * @param topic         主题
     * @param consumerGroup 消费者组
     * @param session       会话（即消费者）
     */
    void unsubscribeDo(String topic, String consumerGroup, Session session);

    /**
     * 执行路由
     *
     * @param message 消息
     */
    void routingDo(MqResolver mr, Message message);

    /**
     * 执行路由
     *
     * @param queue       队列
     * @param message     消息
     * @param tid         事务Id
     * @param qos         质量等级
     * @param sequence    是否为顺序
     * @param expiration  过期时间
     * @param transaction 是否为事务
     * @param times       派发次数
     * @param scheduled   计划时间
     */
    void routingToQueueDo(MqResolver mr, MqQueue queue, Message message, String tid, int qos, boolean sequence, long expiration, boolean transaction, String sender, int times, long scheduled);

    /**
     * 添加事件扩展
     *
     * @param event   事件
     * @param handler 处理
     */
    void doOnEvent(String event, MessageHandler handler);

    /**
     * 保存
     */
    void save();
}