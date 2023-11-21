package org.noear.folkmq.client;

import java.io.IOException;

/**
 * 消息结构体定义
 *
 * @author noear
 * @since 1.0
 */
public interface MqMessage {
    /**
     * 消息内容
     */
    String getContent();

    /**
     * 已派发次数
     */
    int getTimes();

    /**
     * 确认
     */
    void acknowledge(boolean isOk) throws IOException;
}
