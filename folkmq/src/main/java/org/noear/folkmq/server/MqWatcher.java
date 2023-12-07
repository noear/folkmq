package org.noear.folkmq.server;

import org.noear.socketd.transport.core.Message;
import org.noear.socketd.transport.core.Session;

/**
 * 消息观察者
 *
 * @author noear
 * @since 1.0
 */
public interface MqWatcher {
    /**
     * 初始化
     */
    void init(MqServiceInternal serverInternal);

    /**
     * 服务启动之前
     */
    void onStartBefore();

    /**
     * 服务启动之后
     */
    void onStartAfter();

    /**
     * 服务停止之前
     */
    void onStopBefore();

    /**
     * 服务停止之后
     */
    void onStopAfter();

    /**
     * 保存时
     */
    void onSave();

    /**
     * 订阅时
     *
     * @param topic         主题
     * @param consumerGroup 消费者组
     * @param session       会话（即消费者）
     */
    void onSubscribe(String topic, String consumerGroup, Session session);

    /**
     * 取消订阅时
     *
     * @param topic         主题
     * @param consumerGroup 消费者组
     * @param session       会话（即消费者）
     */
    void onUnSubscribe(String topic, String consumerGroup, Session session);

    /**
     * 发布时
     *
     * @param message 消息
     */
    void onPublish(Message message);

    /**
     * 派发时
     *
     * @param topic         主题
     * @param consumerGroup 消费者组
     * @param messageHolder 消息持有人
     */
    void onDistribute(String topic, String consumerGroup, MqMessageHolder messageHolder);

    /**
     * 回执时
     *
     * @param topic         主题
     * @param consumerGroup 消费者组
     * @param messageHolder 消息持有人
     * @param isOk          回执
     */
    void onAcknowledge(String topic, String consumerGroup, MqMessageHolder messageHolder, boolean isOk);
}
