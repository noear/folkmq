package org.noear.folkmq.client;

import java.io.IOException;

/**
 * 收到的消息
 *
 * @author noear
 * @since 1.0
 */
public interface MqMessageReceived extends MqMessage{
    /**
     * 主题
     */
    String getTopic();

    /**
     * 已派发次数
     */
    int getTimes();

    /**
     * 回执
     */
    void acknowledge(boolean isOk) throws IOException;
}
