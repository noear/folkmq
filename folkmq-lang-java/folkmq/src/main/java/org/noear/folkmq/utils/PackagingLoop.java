package org.noear.folkmq.utils;

import java.util.Collection;

/**
 * 打包循环器（一个个添加；打包后批量处理）
 *
 * @author noear
 * @since 1.3
 */
public interface PackagingLoop<Event> {
    /**
     * 设置工作处理
     * */
    void setWorkHandler(PackagingWorkHandler<Event> workHandler);
    /**
     * 设置空闲休息时间
     * */
    void setIdleInterval(long idleInterval);
    /**
     * 设置包装合大小
     * */
    void setPacketSize(int packetSize);

    /**
     * 添加
     * */
    void add(Event event);
    /**
     * 添加一批
     * */
    void addAll(Collection<Event> events);
}
