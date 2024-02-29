package org.noear.folkmq.client;

import java.util.Date;

/**
 * 消息接口
 *
 * @author noear
 * @since 1.0
 */
public interface MqMessageBase {
    /**
     * 发送人
     * */
    String getSender();

    /**
     * 跟踪ID
     */
    String getTid();

    /**
     * 标签
     * */
    String getTag();

    /**
     * 内容
     */
    String getContent();

    /**
     * 过期时间
     */
    Date getExpiration();

    /**
     * 是否事务
     */
    boolean isTransaction();

    /**
     * 是否有序
     */
    boolean isSequence();

    /**
     * 质量等级（0 或 1）
     */
    int getQos();

    /**
     * 获取属性
     */
    String getAttr(String name);
}
