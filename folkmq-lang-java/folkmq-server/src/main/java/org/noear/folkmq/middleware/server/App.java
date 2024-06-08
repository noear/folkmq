package org.noear.folkmq.middleware.server;

import org.noear.folkmq.embedded.MqServerConfig;
import org.noear.socketd.transport.server.ServerConfig;
import org.noear.solon.Solon;
import org.noear.solon.core.event.AppPluginLoadEndEvent;

/**
 * @author noear
 * @since 1.0
 */
public class App {
    public static void main(String[] args) {
        Solon.start(App.class, args, app -> {
            app.onEvent(AppPluginLoadEndEvent.class, e -> {
                app.get("/", ctx -> ctx.redirect(MqServerConfig.path + "/login"));
            });

            app.onEvent(ServerConfig.class, c -> {
                c.readSemaphore(0);
            });
        });
    }
}
