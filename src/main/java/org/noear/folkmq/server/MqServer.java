package org.noear.folkmq.server;

import java.util.concurrent.ExecutorService;

/**
 * @author noear
 * @since 1.0
 */
public interface MqServer {
    /**
     * 配置访问账号
     */
    MqServer addAccess(String accessKey, String accessSecretKey);

    /**
     * 配置派发执行器
     */
    MqServer distributeExecutor(ExecutorService distributeExecutor);

    /**
     * 启动
     */
    MqServer start(int port) throws Exception;

    /**
     * 停止
     */
    MqServer stop();
}
