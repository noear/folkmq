package org.noear.folkmq.broker.middleware;

import org.noear.folkmq.broker.embedded.MqBrokerConfig;
import org.noear.socketd.transport.client.ClientConfig;
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
                app.get("/", ctx -> ctx.redirect(MqBrokerConfig.path + "/login"));
            });

            app.onEvent(ServerConfig.class, c -> {

            });

            app.onEvent(ClientConfig.class, c -> {

            });
        });
    }
}