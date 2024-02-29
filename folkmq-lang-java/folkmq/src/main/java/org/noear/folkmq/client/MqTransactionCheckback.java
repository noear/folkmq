package org.noear.folkmq.client;

/**
 * 事务回查处理
 *
 * @author noear
 * @since 1.2
 */
public interface MqTransactionCheckback {
    /**
     * 检查
     *
     * @param message 消息
     * */
    void check(MqMessageReceived message) throws Exception;
}
