package org.noear.folkmq.server;

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
public class MqServerDefault implements MqServer {
    //服务端监听器
    private final MqServiceListener serverListener;

    //服务端架构
    private final String serverSchema;
    //服务端
    private Server server;
    //服务端配置处理
    private ServerConfigHandler serverConfigHandler;

    public MqServerDefault(String schema, MqServiceListener serverListener) {
        if(StrUtils.isEmpty(schema)){
            schema = "sd:tcp";
        }
        serverSchema = schema;
        if(serverListener == null){
            this.serverListener = new MqServiceListener(false);
        }else{
            this.serverListener = serverListener;
        }
    }

    public MqServerDefault(String schema) {
        this(schema, null);
    }

    public MqServerDefault() {
        this(null);
    }

    /**
     * 服务端配置
     */
    @Override
    public MqServer config(ServerConfigHandler configHandler) {
        serverConfigHandler = configHandler;
        return this;
    }

    /**
     * 配置观察者
     */
    @Override
    public MqServer watcher(MqWatcher watcher) {
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
    public MqServer addAccess(String accessKey, String accessSecretKey) {
        serverListener.addAccess(accessKey, accessSecretKey);
        return this;
    }

    /**
     * 配置访问账号
     *
     * @param accessMap 访问账号集合
     */
    @Override
    public MqServer addAccessAll(Map<String, String> accessMap) {
        serverListener.addAccessAll(accessMap);
        return this;
    }

    /**
     * 启动
     */
    @Override
    public MqServer start(int port) throws Exception {

        //创建 SocketD 服务并配置（使用 tpc 通讯）
        server = SocketD.createServer(serverSchema);

        server.config(c -> c.serialSend(true)
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
    public MqServiceInternal getServerInternal() {
        return serverListener;
    }
}