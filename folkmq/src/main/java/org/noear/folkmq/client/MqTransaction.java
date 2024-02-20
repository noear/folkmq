package org.noear.folkmq.client;

import java.io.IOException;

/**
 * 事务
 *
 * @author noear
 * @since 1.1
 */
public interface MqTransaction {
    /**
     * 事务管理id
     */
    String tmid();

    /**
     * 事务提交
     */
    void commit() throws IOException;

    /**
     * 事务回滚
     */
    void rollback() throws IOException;
}
