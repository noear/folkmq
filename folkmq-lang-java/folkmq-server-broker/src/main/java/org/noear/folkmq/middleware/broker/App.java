package org.noear.folkmq.middleware.broker;

import org.noear.folkmq.middleware.broker.admin.dso.LicenceUtils;
import org.noear.socketd.SocketD;
import org.noear.socketd.transport.client.ClientProvider;
import org.noear.socketd.transport.server.ServerProvider;
import org.noear.solon.Solon;
import org.noear.solon.core.event.AppLoadEndEvent;
import org.noear.solon.core.util.ClassUtil;
import org.noear.solon.core.util.LogUtil;
import org.noear.solon.validation.ValidatorException;

import java.util.List;

/**
 * @author noear
 * @since 1.0
 */
public class App {
    public static void main(String[] args) {
        Solon.start(App.class, args, app -> {
            //加载传传输插件
            List<String> transportList = Solon.cfg().getList("folkmq.transport");
            for (String s1 : transportList) {
                Object p1 = ClassUtil.tryInstance(s1);
                if (p1 instanceof ServerProvider) {
                    SocketD.registerServerProvider((ServerProvider) p1);
                }
                if (p1 instanceof ClientProvider) {
                    SocketD.registerClientProvider((ClientProvider) p1);
                }
            }

            //启用安全停止
            app.cfg().stopSafe(true);

            //加载环境变量
            app.cfg().loadEnv("folkmq.");

            //打印许可证
            LicenceUtils.getGlobal().load();
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