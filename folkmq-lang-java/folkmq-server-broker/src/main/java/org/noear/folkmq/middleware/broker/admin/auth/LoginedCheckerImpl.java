package org.noear.folkmq.middleware.broker.admin.auth;

import org.noear.solon.annotation.Configuration;
import org.noear.solon.core.handle.Context;
import org.noear.solon.validation.annotation.Logined;
import org.noear.solon.validation.annotation.LoginedChecker;

/**
 * @author noear
 * @since 1.0
 */
@Configuration
public class LoginedCheckerImpl implements LoginedChecker {

    @Override
    public boolean check(Logined anno, Context ctx, String userKeyName) {
        return "1".equals(ctx.sessionOrDefault("Logined", ""));
    }
}
