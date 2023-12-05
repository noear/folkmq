package org.noear.folkmq.client;

import org.noear.socketd.transport.core.Message;
import org.noear.socketd.transport.core.Session;

import java.io.IOException;

/**
 * 客户端，内部扩展接口
 *
 * @author noear
 * @since 1.0
 */
public interface MqClientInternal extends MqClient {
    /**
     * 消费回执
     *
     * @param session 会话
     * @param message 收到的消息
     * @param isOk    回执
     */
    void acknowledge(Session session, Message from, MqMessageReceivedImpl message, boolean isOk) throws IOException;
}
