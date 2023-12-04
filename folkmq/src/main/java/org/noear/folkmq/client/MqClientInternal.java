package org.noear.folkmq.client;

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
     * @param message 收到的消息
     * @param isOk    回执
     */
    void acknowledge(MqMessageReceivedImpl message, boolean isOk) throws IOException;
}
