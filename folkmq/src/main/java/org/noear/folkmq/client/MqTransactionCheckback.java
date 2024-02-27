package org.noear.folkmq.client;

/**
 * 事务反向检查
 *
 * @author noear
 * @since 1.2
 */
public interface MqTransactionCheckback {
    /**
     * 检查
     * */
    void check(MqMessageReceived message) throws Exception;
}
