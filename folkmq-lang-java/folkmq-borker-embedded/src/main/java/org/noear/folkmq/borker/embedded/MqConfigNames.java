package org.noear.folkmq.borker.embedded;

/**
 * @author noear
 * @since 1.0
 */
public interface MqConfigNames {
    //管理路径
    String folkmq_path = "folkmq.path";
    //管理密码
    String folkmq_admin = "folkmq.admin";
    //通讯架构
    String folkmq_schema = "folkmq.schema";
    //代理地址
    String folkmq_proxy = "folkmq.proxy";

    //io线程数
    String folkmq_ioThreads = "folkmq.ioThreads";
    //核心线程数
    String folkmq_codecThreads = "folkmq.codecThreads";
    //最大线程数
    String folkmq_exchangeThreads = "folkmq.exchangeThreads";
    //流超时
    String folkmq_streamTimeout = "folkmq.streamTimeout";


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
