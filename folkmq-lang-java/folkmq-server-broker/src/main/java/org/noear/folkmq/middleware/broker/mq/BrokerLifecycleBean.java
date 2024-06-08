package org.noear.folkmq.middleware.broker.mq;

import org.noear.folkmq.FolkMQ;
import org.noear.folkmq.common.MqConstants;
import org.noear.folkmq.middleware.broker.admin.dso.QueueForceService;
import org.noear.folkmq.middleware.broker.common.MqBrokerConfig;
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
import org.noear.solon.core.event.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author noear
 * @since 1.0
 */
@Component
public class BrokerLifecycleBean implements LifecycleBean {
    private static final Logger log = LoggerFactory.getLogger(BrokerLifecycleBean.class);

    @Inject
    private AppContext appContext;

    @Inject
    private QueueForceService queueForceService;

    private Server brokerServerTcp;
    private Server brokerServerWs;
    private BrokerListenerFolkmq brokerListener;

    @Override
    public void start() throws Throwable {
        BrokerFragmentHandler brokerFragmentHandler = new BrokerFragmentHandler();
        brokerListener = new BrokerListenerFolkmq(new BrokerApiHandler(queueForceService))
                .addAccessAll(MqBrokerConfig.getAccessMap());

        brokerServerTcp = SocketD.createServer("sd:tcp")
                .config(c -> {
                    c.port(Solon.cfg().serverPort() + 10000)
                            .serialSend(true)
                            .maxMemoryRatio(0.8F)
                            .streamTimeout(MqBrokerConfig.streamTimeout)
                            .readSemaphore(MqConstants.CLIENT_READ_SEMAPHORE_DEFAULT)
                            .ioThreads(MqBrokerConfig.ioThreads)
                            .codecThreads(MqBrokerConfig.codecThreads)
                            .exchangeThreads(MqBrokerConfig.exchangeThreads)
                            .fragmentHandler(brokerFragmentHandler);

                    EventBus.publish(c);
                })
                .listen(brokerListener)
                .start();


        if (Solon.cfg().getBool("folkmq.websocket", false)) {
            //添加 sd:ws 协议监听支持
            brokerServerWs = SocketD.createServer("sd:ws")
                    .config(c -> {
                        c.port(Solon.cfg().serverPort() + 10001)
                                .serialSend(true)
                                .maxMemoryRatio(0.8F)
                                .streamTimeout(MqBrokerConfig.streamTimeout)
                                .readSemaphore(MqConstants.CLIENT_READ_SEMAPHORE_DEFAULT)
                                .ioThreads(MqBrokerConfig.ioThreads)
                                .codecThreads(MqBrokerConfig.codecThreads)
                                .exchangeThreads(MqBrokerConfig.exchangeThreads)
                                .exchangeExecutor(brokerServerTcp.getConfig().getExchangeExecutor()) //复用通用执行器
                                .fragmentHandler(brokerFragmentHandler);

                        EventBus.publish(c);
                    })
                    .listen(brokerListener)
                    .start();
        }

        appContext.wrapAndPut(BrokerListenerFolkmq.class, brokerListener);

        log.info("Server:main: folkmq-server-broker: Started (SOCKET.D/{}-{}, folkmq/{})",
                SocketD.protocolVersion(),
                SocketD.version(),
                FolkMQ.versionName());
    }

    @Override
    public void prestop() throws Throwable {
        if (brokerListener != null) {
            for (Session session : brokerListener.getSessionAll()) {
                RunUtils.runAndTry(session::closeStarting);
            }
        }
    }

    @Override
    public void stop() throws Throwable {
        if (brokerListener != null) {
            for (Session session : brokerListener.getSessionAll()) {
                RunUtils.runAndTry(session::close);
            }

            brokerListener.stop();
        }

        if (brokerServerTcp != null) {
            brokerServerTcp.stop();
        }

        if (brokerServerWs != null) {
            brokerServerWs.stop();
        }
    }
}