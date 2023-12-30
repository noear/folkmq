package org.noear.folkmq.broker.mq;

import org.noear.folkmq.FolkMQ;
import org.noear.folkmq.broker.common.ConfigNames;
import org.noear.socketd.SocketD;
import org.noear.socketd.broker.BrokerFragmentHandler;
import org.noear.socketd.transport.core.Session;
import org.noear.socketd.transport.server.Server;
import org.noear.socketd.utils.RunUtils;
import org.noear.socketd.utils.StrUtils;
import org.noear.solon.Solon;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.core.AppContext;
import org.noear.solon.core.bean.LifecycleBean;
import org.noear.solon.net.websocket.WebSocketRouter;
import org.noear.solon.net.websocket.socketd.ToSocketdWebSocketListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;

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
    private ToSocketdWebSocketListener webSocketListener;

    private Map<String, String> getAccessMap() {
        Map<String, String> accessMap = Solon.cfg().getMap(ConfigNames.folkmq_access_x);
        accessMap.remove("ak");
        accessMap.remove("sk");

        String ak = Solon.cfg().get(ConfigNames.folkmq_access_ak);
        String sk = Solon.cfg().get(ConfigNames.folkmq_access_sk);

        if (StrUtils.isNotEmpty(ak)) {
            accessMap.put(ak, sk);
        }

        return accessMap;
    }

    @Override
    public void start() throws Throwable {
        brokerListener = new BrokerListenerFolkmq()
                .addAccessAll(getAccessMap());

        brokerServer = SocketD.createServer("sd:tcp")
                .config(c -> c.port(Solon.cfg().serverPort() + 10000)
                        .coreThreads(2)
                        .maxThreads(4)
                        .fragmentHandler(new BrokerFragmentHandler()))
                .listen(brokerListener)
                .start();

        appContext.wrapAndPut(Server.class, brokerServer);
        appContext.wrapAndPut(BrokerListenerFolkmq.class, brokerListener);

        log.info("Server:main: folkmq-broker: Started (SOCKET.D/{}-{}, folkmq/{})",
                SocketD.protocolVersion(),
                SocketD.version(),
                FolkMQ.version());


        if (Solon.app().enableWebSocket()) {
            //添加 sd:ws 协议监听支持
            webSocketListener = new ToSocketdWebSocketListener(brokerServer.getConfig(), brokerListener);
            WebSocketRouter.getInstance().of("/", webSocketListener);
        }
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