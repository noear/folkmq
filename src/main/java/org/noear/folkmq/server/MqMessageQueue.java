package org.noear.folkmq.server;

import org.noear.socketd.transport.core.Session;

/**
 * @author noear
 * @since 1.0
 */
public interface MqMessageQueue {

    /**
     * 添加订阅者
     * */
    void addSubscriber(Session session);

    /**
     * 添加订阅者
     * */
    void removeSubscriber(Session session);

    /**
     * 获取关联身份
     */
    String getIdentity();

    /**
     * 推入消息持有人
     */
    void push(MqMessageHolder messageHolder);
}
