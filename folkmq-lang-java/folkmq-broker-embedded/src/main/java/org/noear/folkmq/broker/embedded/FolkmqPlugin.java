package org.noear.folkmq.broker.embedded;

import org.noear.folkmq.broker.embedded.admin.AdminQueueController;
import org.noear.folkmq.broker.embedded.admin.LoginController;
import org.noear.folkmq.broker.embedded.admin.AdminController;
import org.noear.solon.Solon;
import org.noear.solon.core.AppContext;
import org.noear.solon.core.Plugin;
import org.noear.solon.core.util.ResourceUtil;
import org.noear.solon.validation.ValidatorException;
import org.noear.solon.web.staticfiles.StaticMappings;
import org.noear.solon.web.staticfiles.repository.ClassPathStaticRepository;

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

        //启用安全停止
        Solon.app().cfg().stopSafe(true);

        //加载环境变量
        Solon.app().cfg().loadEnv("folkmq.");

        //登录鉴权跳转
        Solon.app().routerInterceptor(0, ((ctx, mainHandler, chain) -> {
            try {
                chain.doIntercept(ctx, mainHandler);
            } catch (ValidatorException e) {
                ctx.redirect(MqBrokerConfig.path + "/login");
            }
        }));

        //扫描
        context.beanScan(FolkmqPlugin.class);

        //ctl
        Solon.app().add(MqBrokerConfig.path, AdminController.class);
        Solon.app().add(MqBrokerConfig.path, AdminQueueController.class);
        Solon.app().add(MqBrokerConfig.path, LoginController.class);

        //static
        StaticMappings.add(MqBrokerConfig.path + "/", new ClassPathStaticRepository("folkmq/static"));
    }
}