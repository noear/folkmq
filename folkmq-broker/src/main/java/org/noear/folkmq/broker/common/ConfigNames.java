package org.noear.folkmq.broker.common;

/**
 * @author noear
 * @since 1.0
 */
public interface ConfigNames {
    //管理密码
    String folkmq_admin = "folkmq.admin";
    //许可证
    String folkmq_licence = "folkmq.licence";
    //视图队列同步间隔（ms）
    String folkmq_view_queue_syncInterval = "folkmq.view.queue.syncInterval";
    String folkmq_view_queue_syncInterval_def = "5000";

    //访问账号(ak:sk)
    String folkmq_access_x = "folkmq.access.";
}
