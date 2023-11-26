package org.noear.folkmq.client;

import org.noear.socketd.transport.core.Message;

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
     * @param message 消息
     * @param isOk    回执
     */
    void acknowledge(MqMessageImpl message, boolean isOk) throws IOException;
}
