package org.noear.folkmq.client;

import java.util.Date;

/**
 * 消息接口
 *
 * @author noear
 * @since 1.0
 */
public interface IMqMessage {
    /**
     * 事务ID
     */
    String getTid();

    /**
     * 内容
     */
    String getContent();

    /**
     * 过期时间
     * */
    Date getExpiration();

    /**
     * 质量等级（0 或 1）
     */
    int getQos();
}
