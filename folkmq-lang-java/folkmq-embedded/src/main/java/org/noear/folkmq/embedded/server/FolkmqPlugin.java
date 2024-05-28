package org.noear.folkmq.embedded.server;

import org.noear.folkmq.embedded.server.admin.AdminController;
import org.noear.folkmq.embedded.server.admin.AdminQueueController;
import org.noear.folkmq.embedded.server.admin.LoginController;
import org.noear.socketd.SocketD;
import org.noear.socketd.transport.java_websocket.WsNioProvider;
import org.noear.socketd.transport.netty.tcp.TcpNioProvider;
import org.noear.socketd.transport.netty.udp.UdpNioProvider;
import org.noear.solon.Solon;
import org.noear.solon.core.AppContext;
import org.noear.solon.core.Plugin;
import org.noear.solon.core.util.ResourceUtil;
import org.noear.solon.validation.ValidatorException;

/**
 * @author noear
 * @since 1.5
 */
public class FolkmqPlugin implements Plugin {
    @Override
    public void start(AppContext context) throws Throwable {
        //手动注册（避免 spi 失效）
        SocketD.registerServerProvider(new WsNioProvider());
        SocketD.registerClientProvider(new WsNioProvider());
        SocketD.registerServerProvider(new TcpNioProvider());
        SocketD.registerClientProvider(new TcpNioProvider());
        SocketD.registerServerProvider(new UdpNioProvider());
        SocketD.registerClientProvider(new UdpNioProvider());

        //加载配置文件
        Solon.cfg().loadAddIfAbsent(ResourceUtil.getResource("folkmq.yml"));

        //启用安全停止
        Solon.app().cfg().stopSafe(true);

        //加载环境变量
        Solon.app().cfg().loadEnv("folkmq.");

        //登录鉴权跳转
        Solon.app().routerInterceptor(0, ((ctx, mainHandler, chain) -> {
            try {
                chain.doIntercept(ctx, mainHandler);
            } catch (ValidatorException e) {
                ctx.redirect("/login");
            }
        }));

        //扫描
        context.beanScan(FolkmqPlugin.class);
        Solon.app().add("/folkmq/", AdminController.class);
        Solon.app().add("/folkmq/", AdminQueueController.class);
        Solon.app().add("/folkmq/", LoginController.class);
    }
}
