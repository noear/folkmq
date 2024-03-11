package org.noear.folkmq.broker;

import org.noear.folkmq.broker.admin.dso.LicenceUtils;
import org.noear.socketd.SocketD;
import org.noear.socketd.transport.java_websocket.WsNioProvider;
import org.noear.socketd.transport.netty.tcp.TcpNioProvider;
import org.noear.socketd.transport.netty.udp.UdpNioProvider;
import org.noear.solon.Solon;
import org.noear.solon.core.event.AppLoadEndEvent;
import org.noear.solon.core.util.LogUtil;
import org.noear.solon.validation.ValidatorException;

/**
 * @author noear
 * @since 1.0
 */
public class App {
    public static void main(String[] args) {
        Solon.start(App.class, args, app -> {
            //手动注册（避免 spi 失效）
            SocketD.registerServerProvider(new WsNioProvider());
            SocketD.registerClientProvider(new WsNioProvider());
            SocketD.registerServerProvider(new TcpNioProvider());
            SocketD.registerClientProvider(new TcpNioProvider());
            SocketD.registerServerProvider(new UdpNioProvider());
            SocketD.registerClientProvider(new UdpNioProvider());

            //启用安全停止
            app.cfg().stopSafe(true);

            //加载环境变量
            app.cfg().loadEnv("folkmq.");

            //打印语可证
            app.onEvent(AppLoadEndEvent.class, Integer.MAX_VALUE, e -> {
                LogUtil.global().info(LicenceUtils.getGlobal().getDescription());
            });

            //登录鉴权跳转
            app.routerInterceptor(0, ((ctx, mainHandler, chain) -> {
                try {
                    chain.doIntercept(ctx, mainHandler);
                } catch (ValidatorException e) {
                    ctx.redirect("/login");
                }
            }));
        });
    }
}