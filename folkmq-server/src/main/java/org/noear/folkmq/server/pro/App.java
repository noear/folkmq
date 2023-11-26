package org.noear.folkmq.server.pro;

import org.noear.solon.Solon;

/**
 * @author noear
 * @since 1.0
 */
public class App {
    public static void main(String[] args) {
        Solon.start(App.class, args, app->{
            Solon.cfg().loadEnv("folkmq.");
        });
    }
}
