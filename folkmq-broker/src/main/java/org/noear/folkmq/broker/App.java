package org.noear.folkmq.broker;

import org.noear.folkmq.broker.admin.dso.LicenceUtils;
import org.noear.socketd.utils.RunUtils;
import org.noear.solon.Solon;
import org.noear.solon.validation.ValidatorException;

/**
 * @author noear
 * @since 1.0
 */
public class App {
    public static void main(String[] args) {
        Solon.start(App.class, args, app -> {
            //加载环境变量
            app.cfg().loadEnv("folkmq.");

            //登录鉴权跳转
            app.routerInterceptor(0, ((ctx, mainHandler, chain) -> {
                try {
                    chain.doIntercept(ctx, mainHandler);
                } catch (ValidatorException e) {
                    ctx.redirect("/login");
                }
            }));
        });

        //授权自动检测
        if (LicenceUtils.isValid()) {
            RunUtils.runAndTry(LicenceUtils::auth);
        }
    }
}
