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
     * 获取版本代号（用于控制元信息版本）
     */
    public static int versionCode() {
        return 2;
    }

    /**
     * 获取版本代号并转为字符串
     */
    public static String versionCodeAsString() {
        return String.valueOf(versionCode());
    }

    /**
     * 获取版本名称
     */
    public static String versionName() {
        return "1.4.1";
    }

    /**
     * 创建服务端
     */
    public static MqServer createServer() {
        return new MqServerDefault();
    }

    /**
     * 创建服务端
     *
     * @param schema 指定架构
     */
    public static MqServer createServer(String schema) {
        return new MqServerDefault(schema);
    }

    /**
     * 创建客户端
     */
    public static MqClient createClient(String... serverUrls) {
        return new MqClientDefault(serverUrls);
    }
}
