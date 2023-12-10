package org.noear.folkmq.broker.mq;

import org.noear.folkmq.broker.common.ConfigNames;
import org.noear.socketd.SocketD;
import org.noear.socketd.broker.BrokerFragmentHandler;
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
    private BrokerListenerFolkmq brokerListener;

    @Override
    public void start() throws Throwable {
        brokerListener = new BrokerListenerFolkmq()
                .addAccessAll(Solon.cfg().getMap(ConfigNames.folkmq_access_x));

        brokerServer = SocketD.createServer("sd:tcp")
                .config(c -> c.port(Solon.cfg().serverPort() + 10000)
                        .maxThreads(c.getCoreThreads() * 4) //默认为8
                        .fragmentHandler(new BrokerFragmentHandler()))
                .listen(brokerListener)
                .start();

        appContext.wrapAndPut(BrokerListenerFolkmq.class, brokerListener);
    }

    @Override
    public void stop() throws Throwable {
        if (brokerServer != null) {
            brokerServer.stop();
        }
    }
}
