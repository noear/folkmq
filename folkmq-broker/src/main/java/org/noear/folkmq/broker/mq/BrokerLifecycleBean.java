package org.noear.folkmq.broker.mq;

import org.noear.folkmq.FolkMQ;
import org.noear.folkmq.broker.admin.dso.QueueForceService;
import org.noear.folkmq.broker.common.MqBrokerConfig;
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
import org.noear.solon.core.event.AppPrestopEndEvent;
import org.noear.solon.core.event.EventListener;
import org.noear.solon.net.websocket.socketd.ToSocketdWebSocketListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author noear
 * @since 1.0
 */
@Component
public class BrokerLifecycleBean implements LifecycleBean , EventListener<AppPrestopEndEvent> {
    private static final Logger log = LoggerFactory.getLogger(BrokerLifecycleBean.class);

    @Inject
    private AppContext appContext;

    @Inject
    private QueueForceService queueForceService;

    private Server brokerServerTcp;
    private Server brokerServerWs;
    private BrokerListenerFolkmq brokerListener;
    private ToSocketdWebSocketListener webSocketListener;

    @Override
    public void start() throws Throwable {
        BrokerFragmentHandler brokerFragmentHandler = new BrokerFragmentHandler();
        brokerListener = new BrokerListenerFolkmq(new BrokerApiHandler(queueForceService))
                .addAccessAll(MqBrokerConfig.getAccessMap());

        brokerServerTcp = SocketD.createServer("sd:tcp")
                .config(c -> c.port(Solon.cfg().serverPort() + 10000)
                        .sequenceSend(true)
                        .ioThreads(MqBrokerConfig.ioThreads)
                        .codecThreads(MqBrokerConfig.codecThreads)
                        .exchangeThreads(MqBrokerConfig.exchangeThreads)
                        .fragmentHandler(brokerFragmentHandler))
                .listen(brokerListener)
                .start();


        if (Solon.cfg().getBool("folkmq.websocket", false)) {
            //添加 sd:ws 协议监听支持
            brokerServerWs = SocketD.createServer("sd:ws")
                    .config(c -> c.port(Solon.cfg().serverPort() + 10001)
                            .sequenceSend(true)
                            .ioThreads(MqBrokerConfig.ioThreads)
                            .codecThreads(MqBrokerConfig.codecThreads)
                            .exchangeThreads(MqBrokerConfig.exchangeThreads)
                            .exchangeExecutor(brokerServerTcp.getConfig().getExchangeExecutor()) //复用通用执行器
                            .fragmentHandler(brokerFragmentHandler))
                    .listen(brokerListener)
                    .start();
        }

        appContext.wrapAndPut(BrokerListenerFolkmq.class, brokerListener);

        log.info("Server:main: folkmq-broker: Started (SOCKET.D/{}-{}, folkmq/{})",
                SocketD.protocolVersion(),
                SocketD.version(),
                FolkMQ.version());
    }

    @Override
    public void stop() throws Throwable {
        if (brokerListener != null) {
            for (Session session : brokerListener.getSessionAll()) {
                RunUtils.runAndTry(session::close);
            }
        }

        if (brokerServerTcp != null) {
            brokerServerTcp.stop();
        }

        if (brokerServerWs != null) {
            brokerServerWs.stop();
        }
    }

    @Override
    public void onEvent(AppPrestopEndEvent appPrestopEndEvent) throws Throwable {
        if (brokerListener != null) {
            for (Session session : brokerListener.getSessionAll()) {
                RunUtils.runAndTry(session::closeStarting);
            }
        }
    }
}