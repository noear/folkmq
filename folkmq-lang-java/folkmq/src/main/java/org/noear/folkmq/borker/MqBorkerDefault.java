package org.noear.folkmq.borker;

import org.noear.folkmq.common.MqConstants;
import org.noear.socketd.SocketD;
import org.noear.socketd.transport.server.Server;
import org.noear.socketd.transport.server.ServerConfigHandler;
import org.noear.socketd.utils.StrUtils;

import java.util.*;

/**
 * 消息服务端默认实现
 *
 * @author noear
 * @since 1.0
 */
public class MqBorkerDefault implements MqBorker {
    //服务端监听器
    private final MqBorkerListener serverListener;

    //服务端架构
    private final String serverSchema;
    //服务端
    private Server server;
    //服务端配置处理
    private ServerConfigHandler serverConfigHandler;

    public MqBorkerDefault(String schema, MqBorkerListener serverListener) {
        if (StrUtils.isEmpty(schema)) {
            this.serverSchema = "sd:tcp";
        } else {
            this.serverSchema = schema;
        }

        if (serverListener == null) {
            this.serverListener = new MqBorkerListener(false);
        } else {
            this.serverListener = serverListener;
        }
    }

    public MqBorkerDefault(String schema) {
        this(schema, null);
    }

    public MqBorkerDefault() {
        this(null, null);
    }

    /**
     * 服务端配置
     */
    @Override
    public MqBorker config(ServerConfigHandler configHandler) {
        serverConfigHandler = configHandler;
        return this;
    }

    /**
     * 配置观察者
     */
    @Override
    public MqBorker watcher(MqWatcher watcher) {
        serverListener.watcher(watcher);

        return this;
    }

    /**
     * 配置访问账号
     *
     * @param accessKey       访问者身份
     * @param accessSecretKey 访问者密钥
     */
    @Override
    public MqBorker addAccess(String accessKey, String accessSecretKey) {
        serverListener.addAccess(accessKey, accessSecretKey);
        return this;
    }

    /**
     * 配置访问账号
     *
     * @param accessMap 访问账号集合
     */
    @Override
    public MqBorker addAccessAll(Map<String, String> accessMap) {
        serverListener.addAccessAll(accessMap);
        return this;
    }

    /**
     * 启动
     */
    @Override
    public MqBorker start(int port) throws Exception {

        //创建 SocketD 服务并配置（使用 tpc 通讯）
        server = SocketD.createServer(serverSchema);

        server.config(c -> c.serialSend(true)
                .maxMemoryRatio(0.8F)
                .streamTimeout(MqConstants.SERVER_STREAM_TIMEOUT_DEFAULT)
                .ioThreads(1)
                .codecThreads(1)
                .exchangeThreads(1));

        //配置
        if (serverConfigHandler != null) {
            server.config(serverConfigHandler);
        }

        server.config(c -> c.port(port)).listen(serverListener);

        //开始
        serverListener.start(() -> {
            //启动
            server.start();
        });

        return this;
    }

    @Override
    public void prestop() {
        server.prestop();
    }

    /**
     * 停止
     */
    @Override
    public void stop() {
        serverListener.stop(() -> {
            //停止
            server.stop();
        });
    }

    @Override
    public MqBorkerInternal getServerInternal() {
        return serverListener;
    }
}