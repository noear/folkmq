package org.noear.folkmq.client;

/**
 * 请求处理器
 *
 * @author noear
 * @since 1.2
 */
@FunctionalInterface
public interface MqRequestHandler {
    /**
     * 请求时
     *
     * @param message 收到的消息
     */
    void onRequest(MqMessageReceived message) throws Exception;
}
