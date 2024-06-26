package org.noear.folkmq.client;

import org.noear.socketd.transport.core.Entity;
import org.noear.socketd.transport.core.Session;

import java.io.IOException;
import java.util.List;

/**
 * 客户端，内部扩展接口
 *
 * @author noear
 * @since 1.0
 */
public interface MqClientInternal extends MqClient {
    /**
     * 发布二次提交
     *
     * @param tmid       事务管理id
     * @param keyAry     消息主键集合
     * @param isRollback 是否回滚
     */
    void publish2(String tmid, List<String> keyAry, boolean isRollback) throws IOException;

    /**
     * 消息答复
     *
     * @param session 会话
     * @param message 收到的消息
     * @param isOk    回执
     * @param entity  实体
     */
    void reply(Session session, MqMessageReceivedImpl message, boolean isOk, Entity entity) throws IOException;
}
