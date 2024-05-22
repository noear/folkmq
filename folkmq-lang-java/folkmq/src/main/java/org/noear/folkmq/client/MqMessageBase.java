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
     */
    String getSender();

    /**
     * 主键
     *
     * @deprecated 1.4
     */
    @Deprecated
    default String getTid() {
        return getKey();
    }

    /**
     * 主键
     */
    String getKey();

    /**
     * 标签
     */
    String getTag();

    /**
     * 数据
     */
    byte[] getBody();

    /**
     * 过期时间
     */
    Date getExpiration();

    /**
     * 是否事务
     */
    boolean isTransaction();

    /**
     * 是否广播
     */
    boolean isBroadcast();

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