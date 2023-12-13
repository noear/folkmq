package org.noear.folkmq.broker.mq;

import org.noear.folkmq.FolkMQ;
import org.noear.folkmq.broker.common.ConfigNames;
import org.noear.socketd.SocketD;
import org.noear.socketd.broker.BrokerFragmentHandler;
import org.noear.socketd.transport.core.Session;
import org.noear.socketd.transport.server.Server;
import org.noear.socketd.utils.RunUtils;
import org.noear.solon.Solon;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.core.AppContext;
import org.noear.solon.core.bean.LifecycleBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * @author noear
 * @since 1.0
 */
@Component
public class BrokerLifecycleBean implements LifecycleBean {
    private static final Logger log = LoggerFactory.getLogger(BrokerLifecycleBean.class);

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

        log.info("Server:main: folkmq-broker: Started (SOCKET.D/{}-{}, folkmq/{})",
                SocketD.protocolVersion(),
                SocketD.version(),
                FolkMQ.version());
    }

    @Override
    public void stop() throws Throwable {
        if (brokerListener != null) {
            Collection<String> nameAll = brokerListener.getNameAll();
            for (String name : nameAll) {
                Collection<Session> sessions = brokerListener.getPlayerAll(name);
                for (Session session : sessions) {
                    RunUtils.runAndTry(session::close);
                }
            }
        }

        if (brokerServer != null) {
            brokerServer.stop();
        }
    }
}
