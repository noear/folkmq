package org.noear.folkmq.broker;

import org.noear.socketd.transport.server.ServerConfigHandler;

import java.util.Map;

/**
 * 消息服务端
 *
 * @author noear
 * @since 1.0
 */
public interface MqBorker {
    /**
     * 服务端配置
     */
    MqBorker config(ServerConfigHandler configHandler);

    /**
     * 配置观察者
     */
    MqBorker watcher(MqWatcher watcher);

    /**
     * 配置访问账号
     *
     * @param ak 访问者身份
     * @param sk 访问者密钥
     */
    MqBorker addAccess(String ak, String sk);

    /**
     * 配置访问账号
     *
     * @param accessMap 访问账号集合
     */
    MqBorker addAccessAll(Map<String, String> accessMap);

    /**
     * 启动
     */
    MqBorker start(int port) throws Exception;

    /**
     * 预停止
     * */
    void prestop();

    /**
     * 停止
     */
    void stop();


    /**
     * 获取内部服务
     */
    MqBorkerInternal getServerInternal();
}
