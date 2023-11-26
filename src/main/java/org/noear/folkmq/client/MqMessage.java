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
     * 事务ID
     */
    String getTid();

    /**
     * 主题
     */
    String getTopic();

    /**
     * 内容
     */
    String getContent();

    /**
     * 质量等级（0 或 1）
     */
    int getQos();

    /**
     * 已派发次数
     */
    int getTimes();

    /**
     * 回执
     */
    void acknowledge(boolean isOk) throws IOException;
}
