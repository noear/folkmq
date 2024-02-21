package org.noear.folkmq.client;

/**
 * 请求监听器
 *
 * @author noear
 * @since 1.2
 */
@FunctionalInterface
public interface MqRequestListener {
    /**
     * 请求时
     *
     * @param message 收到的消息
     */
    void onRequest(MqMessageReceived message) throws Exception;
}
