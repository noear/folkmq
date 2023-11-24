package org.noear.folkmq.server;

import java.util.concurrent.ExecutorService;

/**
 * 消息服务端
 *
 * @author noear
 * @since 1.0
 */
public interface MqServer {
    /**
     * 配置访问账号
     *
     * @param accessKey       访问者身份
     * @param accessSecretKey 访问者密钥
     */
    MqServer addAccess(String accessKey, String accessSecretKey);

    /**
     * 配置派发执行器
     *
     * @param distributeExecutor 线程池
     */
    MqServer distributeExecutor(ExecutorService distributeExecutor);

    /**
     * 启动
     */
    MqServer start(int port) throws Exception;

    /**
     * 停止
     */
    void stop();
}
