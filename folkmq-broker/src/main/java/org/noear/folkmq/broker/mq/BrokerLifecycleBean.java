package org.noear.folkmq.broker.mq;

import org.noear.socketd.SocketD;
import org.noear.socketd.broker.BrokerFragmentHandler;
import org.noear.socketd.broker.BrokerListener;
import org.noear.socketd.transport.server.Server;
import org.noear.solon.Solon;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.core.AppContext;
import org.noear.solon.core.bean.LifecycleBean;

/**
 * @author noear
 * @since 1.0
 */
@Component
public class BrokerLifecycleBean implements LifecycleBean {
    @Inject
    private AppContext appContext;

    private Server brokerServer;
    private BrokerListener brokerListener;

    @Override
    public void start() throws Throwable {
        brokerListener = new BrokerListenerFolkmq()
                .addAccessAll(Solon.cfg().getMap("folkmq.access."));

        brokerServer = SocketD.createServer("sd:tcp")
                .config(c -> c.port(Solon.cfg().serverPort() + 10000)
                        .maxThreads(c.getCoreThreads() * 4) //默认为8
                        .fragmentHandler(new BrokerFragmentHandler()))
                .listen(brokerListener)
                .start();

        appContext.wrapAndPut(BrokerListener.class, brokerListener);
    }

    @Override
    public void stop() throws Throwable {
        if (brokerServer != null) {
            brokerServer.stop();
        }
    }
}
