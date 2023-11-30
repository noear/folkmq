package org.noear.folkmq.broker.mq;

import org.noear.socketd.SocketD;
import org.noear.socketd.broker.BrokerFragmentHandler;
import org.noear.socketd.transport.server.Server;
import org.noear.solon.Solon;
import org.noear.solon.annotation.Component;
import org.noear.solon.core.bean.LifecycleBean;

/**
 * @author noear
 * @since 1.0
 */
@Component
public class BrokerLifecycleBean implements LifecycleBean {
    Server server;

    @Override
    public void start() throws Throwable {
        server = SocketD.createServer("sd:tcp")
                .config(c -> c.port(Solon.cfg().serverPort() + 10000)
                        .maxThreads(c.getCoreThreads() * 4) //默认为8
                        .fragmentHandler(new BrokerFragmentHandler()))
                .listen(new BrokerListenerFolkmq())
                .start();
    }

    @Override
    public void stop() throws Throwable {
        if (server != null) {
            server.stop();
        }
    }
}
