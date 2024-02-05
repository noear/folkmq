package org.noear.folkmq.server.pro.common;

/**
 * @author noear
 * @since 1.0
 */
public interface ConfigNames {
    //管理密码
    String folkmq_admin = "folkmq.admin";

    //经纪人地址
    String folkmq_broker = "folkmq.broker";

    //快照相关
    String folkmq_snapshot_enable = "folkmq.snapshot.enable";
    String folkmq_snapshot_save900 = "folkmq.snapshot.save900";
    String folkmq_snapshot_save300 = "folkmq.snapshot.save300";
    String folkmq_snapshot_save100 = "folkmq.snapshot.save100";

    //访问账号(ak:sk) //弃用（改为单账号，用户好接受）
    String folkmq_access_x = "folkmq.access.";
    //访问账号(ak:sk)
    String folkmq_access_ak = "folkmq.access.ak";
    String folkmq_access_sk = "folkmq.access.sk";

    //接口请求令牌
    String folkmq_api_token = "folkmq.api.token";
}
