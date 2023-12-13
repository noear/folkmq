package org.noear.folkmq;

import org.noear.folkmq.client.MqClient;
import org.noear.folkmq.client.MqClientDefault;
import org.noear.folkmq.server.MqServer;
import org.noear.folkmq.server.MqServerDefault;

/**
 * @author noear
 * @since 1.0
 */
public class FolkMQ {
    /**
     * 获取版本
     */
    public static String version() {
        return "1.0.20";
    }

    /**
     * 创建服务端
     */
    public static MqServer createServer() {
        return new MqServerDefault();
    }

    /**
     * 创建客户端
     */
    public static MqClient createClient(String... serverUrls) {
        return new MqClientDefault(serverUrls);
    }
}
