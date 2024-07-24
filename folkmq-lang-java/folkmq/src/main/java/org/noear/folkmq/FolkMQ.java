package org.noear.folkmq;

import org.noear.folkmq.client.MqClient;
import org.noear.folkmq.client.MqClientDefault;
import org.noear.folkmq.broker.MqBorker;
import org.noear.folkmq.broker.MqBorkerDefault;

/**
 * @author noear
 * @since 1.0
 */
public class FolkMQ {
    /**
     * 获取版本代号（用于控制元信息版本）
     */
    public static int versionCode() {
        return 3;
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
        return "1.7.6";
    }

    /**
     * 创建服务端
     */
    public static MqBorker createBorker() {
        return new MqBorkerDefault();
    }

    /**
     * 创建服务端
     *
     * @param schema 指定架构
     */
    public static MqBorker createBorker(String schema) {
        return new MqBorkerDefault(schema);
    }

    /**
     * 创建客户端
     */
    public static MqClient createClient(String... serverUrls) {
        return new MqClientDefault(serverUrls);
    }
}
