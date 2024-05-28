package org.noear.folkmq.middleware.server;

import org.noear.solon.Solon;

/**
 * @author noear
 * @since 1.0
 */
public class App {
    public static void main(String[] args) {
        Solon.start(App.class, args, app -> {
            app.get("/", ctx -> ctx.redirect("/folkmq/login"));
        });
    }
}
