package org.noear.folkmq.embedded;

import org.noear.folkmq.embedded.admin.AdminController;
import org.noear.folkmq.embedded.admin.AdminQueueController;
import org.noear.folkmq.embedded.admin.LoginController;
import org.noear.socketd.SocketD;
import org.noear.socketd.transport.client.ClientProvider;
import org.noear.socketd.transport.server.ServerProvider;
import org.noear.solon.Solon;
import org.noear.solon.core.AppContext;
import org.noear.solon.core.Plugin;
import org.noear.solon.core.util.ClassUtil;
import org.noear.solon.core.util.ResourceUtil;
import org.noear.solon.validation.ValidatorException;

import java.util.List;

/**
 * @author noear
 * @since 1.5
 */
public class FolkmqPlugin implements Plugin {
    @Override
    public void start(AppContext context) throws Throwable {
        //加载配置文件
        Solon.cfg().loadAdd(ResourceUtil.getResource("folkmq.yml"));
        Solon.cfg().loadAdd(ResourceUtil.findResource("./data/folkmq.yml"));

        //加载默认配置文件
        Solon.cfg().loadAddIfAbsent(ResourceUtil.getResource("folkmq-def.yml"));

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
        Solon.app().add(MqServerConfig.path, AdminController.class);
        Solon.app().add(MqServerConfig.path, AdminQueueController.class);
        Solon.app().add(MqServerConfig.path, LoginController.class);
    }
}