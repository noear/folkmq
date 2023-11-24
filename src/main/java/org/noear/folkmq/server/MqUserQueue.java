package org.noear.folkmq.server;

import org.noear.socketd.transport.core.Session;

/**
 * @author noear
 * @since 1.0
 */
public interface MqUserQueue {

    /**
     * 添加用户会话
     * */
    void addSession(Session session);

    /**
     * 移除用户会话
     * */
    void removeSession(Session session);

    /**
     * 获取用户
     */
    String getUser();

    /**
     * 推入消息
     */
    void push(MqMessageHolder messageHolder);
}
