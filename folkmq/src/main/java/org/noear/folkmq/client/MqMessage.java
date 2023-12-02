package org.noear.folkmq.client;

import java.util.Date;

/**
 * 消息
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
     * 内容
     */
    String getContent();

    /**
     * 定时时间
     */
    Date getScheduled();

    /**
     * 质量等级（0 或 1）
     */
    int getQos();
}
