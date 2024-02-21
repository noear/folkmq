package org.noear.folkmq.client;

/**
 * 请求响应器
 *
 * @author noear
 * @since 1.2
 */
@FunctionalInterface
public interface MqResponder {
    /**
     * 请求时
     *
     * @param message 收到的消息
     */
    void onRequest(MqMessageReceived message) throws Exception;
}
