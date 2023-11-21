package org.noear.folkmq.server;

/**
 * @author noear
 * @since 1.0
 */
public interface MqMessageQueue {
    /**
     * 获取关联身份
     */
    String getIdentity();

    /**
     * 添加消息持有人
     */
    void add(MqMessageHolder messageHolder);
}
