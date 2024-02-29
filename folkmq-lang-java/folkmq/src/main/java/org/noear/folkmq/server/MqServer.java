package org.noear.folkmq.server;

import org.noear.socketd.transport.server.ServerConfigHandler;

import java.util.Map;

/**
 * 消息服务端
 *
 * @author noear
 * @since 1.0
 */
public interface MqServer {
    /**
     * 服务端配置
     */
    MqServer config(ServerConfigHandler configHandler);

    /**
     * 配置观察者
     */
    MqServer watcher(MqWatcher watcher);

    /**
     * 配置访问账号
     *
     * @param ak 访问者身份
     * @param sk 访问者密钥
     */
    MqServer addAccess(String ak, String sk);

    /**
     * 配置访问账号
     *
     * @param accessMap 访问账号集合
     */
    MqServer addAccessAll(Map<String, String> accessMap);

    /**
     * 启动
     */
    MqServer start(int port) throws Exception;

    /**
     * 停止
     */
    void stop();


    /**
     * 获取内部服务
     */
    MqServiceInternal getServerInternal();
}
