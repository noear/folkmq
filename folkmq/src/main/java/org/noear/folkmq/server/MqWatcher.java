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
    void init(MqServerInternal serverInternal);

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
     */
    void onSubscribe(String topic, String consumer, Session session);

    /**
     * 取消订阅时
     */
    void onUnSubscribe(String topic, String consumer, Session session);

    /**
     * 发布时
     */
    void onPublish(Message message);

    /**
     * 派发时
     */
    void onDistribute(String consumer, MqMessageHolder messageHolder);

    /**
     * 回执时
     */
    void onAcknowledge(String consumer, MqMessageHolder messageHolder, boolean isOk);
}
