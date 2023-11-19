package org.noear.folkmq.server;

import java.io.IOException;

/**
 * @author noear
 * @since 1.0
 */
public interface MqServer {
    void start(int port) throws Exception;
    void stop();
}
