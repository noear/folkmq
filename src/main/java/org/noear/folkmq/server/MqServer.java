package org.noear.folkmq.server;

/**
 * @author noear
 * @since 1.0
 */
public interface MqServer {
    MqServer addAccess(String accessKey, String accessSecretKey);
    MqServer start(int port) throws Exception;
    MqServer stop();
}
