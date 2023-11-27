package org.noear.folkmq.server;

import org.noear.socketd.transport.core.Message;
import org.noear.socketd.transport.core.Session;

/**
 * 消息持久化（持久化定位为副本，只要重启时能恢复订阅关系与消息即可）
 * <br/>
 * 关键：onStart.., onStop.., onSubscribe, onPublish。
 * 提示：onSubscribe, onPublish 做同步处理（可靠性高），做异步处理（性能高）。具体看场景需求
 *
 * @author noear
 * @since 1.0
 */
public interface MqPersistent {
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
