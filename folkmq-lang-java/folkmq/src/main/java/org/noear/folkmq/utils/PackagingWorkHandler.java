package org.noear.folkmq.utils;

import java.util.List;

/**
 * 打包工作处理
 *
 * @author noear
 * @since 1.3
 */
public interface PackagingWorkHandler<Event> {
    void onEvents(List<Event> list) throws Exception;
}
