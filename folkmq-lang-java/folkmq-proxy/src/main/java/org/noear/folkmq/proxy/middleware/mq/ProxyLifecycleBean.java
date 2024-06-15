package org.noear.folkmq.proxy.middleware.mq;

import org.noear.folkmq.FolkMQ;
import org.noear.folkmq.proxy.middleware.admin.dso.QueueForceService;
import org.noear.folkmq.proxy.middleware.common.MqProxyConfig;
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
 * 代理生命周期
 *
 * @author noear
 * @since 1.0
 */
@Component
public class ProxyLifecycleBean implements LifecycleBean {
    private static final Logger log = LoggerFactory.getLogger(ProxyLifecycleBean.class);

    @Inject
    private AppContext appContext;

    @Inject
    private QueueForceService queueForceService;

    private Server proxyServerTcp;
    private Server proxyServerWs;
    private FolkmqProxyListener proxyListener;

    @Override
    public void start() throws Throwable {
        BrokerFragmentHandler brokerFragmentHandler = new BrokerFragmentHandler();
        proxyListener = new FolkmqProxyListener(new ProxyApiHandler(queueForceService))
                .addAccessAll(MqProxyConfig.getAccessMap());

        proxyServerTcp = SocketD.createServer("sd:tcp")
                .config(c -> {
                    c.port(Solon.cfg().serverPort() + 10000)
                            .serialSend(true)
                            .maxMemoryRatio(0.8F)
                            .streamTimeout(MqProxyConfig.streamTimeout)
                            .ioThreads(MqProxyConfig.ioThreads)
                            .codecThreads(MqProxyConfig.codecThreads)
                            .exchangeThreads(MqProxyConfig.exchangeThreads)
                            .fragmentHandler(brokerFragmentHandler);

                    EventBus.publish(c);
                })
                .listen(proxyListener)
                .start();


        if (Solon.cfg().getBool("folkmq.websocket", false)) {
            //添加 sd:ws 协议监听支持
            proxyServerWs = SocketD.createServer("sd:ws")
                    .config(c -> {
                        c.port(Solon.cfg().serverPort() + 10001)
                                .serialSend(true)
                                .maxMemoryRatio(0.8F)
                                .streamTimeout(MqProxyConfig.streamTimeout)
                                .ioThreads(MqProxyConfig.ioThreads)
                                .codecThreads(MqProxyConfig.codecThreads)
                                .exchangeThreads(MqProxyConfig.exchangeThreads)
                                .exchangeExecutor(proxyServerTcp.getConfig().getExchangeExecutor()) //复用通用执行器
                                .fragmentHandler(brokerFragmentHandler);

                        EventBus.publish(c);
                    })
                    .listen(proxyListener)
                    .start();
        }

        //启动
        proxyListener.start();

        //注册
        appContext.wrapAndPut(FolkmqProxyListener.class, proxyListener);

        log.info("Server:main: folkmq-proxy: Started (SOCKET.D/{}-{}, folkmq/{})",
                SocketD.protocolVersion(),
                SocketD.version(),
                FolkMQ.versionName());
    }

    @Override
    public void prestop() throws Throwable {
        if (proxyListener != null) {
            for (Session session : proxyListener.getSessionAll()) {
                RunUtils.runAndTry(session::closeStarting);
            }
        }
    }

    @Override
    public void stop() throws Throwable {
        if (proxyListener != null) {
            for (Session session : proxyListener.getSessionAll()) {
                RunUtils.runAndTry(session::close);
            }

            proxyListener.stop();
        }

        if (proxyServerTcp != null) {
            proxyServerTcp.stop();
        }

        if (proxyServerWs != null) {
            proxyServerWs.stop();
        }
    }
}