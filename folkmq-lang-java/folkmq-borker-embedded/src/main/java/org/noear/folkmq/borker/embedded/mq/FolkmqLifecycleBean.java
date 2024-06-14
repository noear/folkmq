package org.noear.folkmq.borker.embedded.mq;

import org.noear.folkmq.FolkMQ;
import org.noear.folkmq.borker.embedded.admin.dso.QueueForceService;
import org.noear.folkmq.borker.embedded.admin.dso.ViewUtils;
import org.noear.folkmq.borker.embedded.admin.model.ServerInfoVo;
import org.noear.folkmq.common.MqConstants;
import org.noear.folkmq.borker.embedded.MqConfigNames;
import org.noear.folkmq.borker.embedded.MqServerConfig;
import org.noear.folkmq.borker.MqBorker;
import org.noear.folkmq.borker.MqBorkerInternal;
import org.noear.folkmq.borker.MqBorkerListener;
import org.noear.folkmq.borker.watcher.MqWatcherSnapshotPlus;
import org.noear.snack.ONode;
import org.noear.socketd.SocketD;
import org.noear.socketd.cluster.ClusterClientSession;
import org.noear.socketd.transport.core.entity.StringEntity;
import org.noear.socketd.utils.MemoryUtils;
import org.noear.solon.Solon;
import org.noear.solon.Utils;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.core.AppContext;
import org.noear.solon.core.bean.LifecycleBean;
import org.noear.solon.core.event.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author noear
 * @since 1.0
 */
@Component
public class FolkmqLifecycleBean implements LifecycleBean {
    private static final Logger log = LoggerFactory.getLogger(FolkmqLifecycleBean.class);
    private static boolean isStandalone;

    public static boolean isStandalone() {
        return isStandalone;
    }

    @Inject
    private AppContext appContext;

    @Inject
    private QueueForceService queueForceService;

    private MqBorker localServer;

    private MqBorkerListener brokerServiceListener;
    private ClusterClientSession brokerSession;
    private MqWatcherSnapshotPlus snapshotPlus;

    @Override
    public void start() throws Throwable {
        //初始化快照持久化
        snapshotPlus = new MqWatcherSnapshotPlus();
        snapshotPlus.save900Condition(MqServerConfig.save900);
        snapshotPlus.save300Condition(MqServerConfig.save300);
        snapshotPlus.save100Condition(MqServerConfig.save100);

        appContext.wrapAndPut(MqWatcherSnapshotPlus.class, snapshotPlus);

        if (Utils.isEmpty(MqServerConfig.proxyServer)) {
            isStandalone = true;
            startLocalServerMode(snapshotPlus);
        } else {
            isStandalone = false;
            startBrokerSession(MqServerConfig.proxyServer, snapshotPlus);
        }

        log.info("Server:main: folkmq-server: Started (SOCKET.D/{}-{}, folkmq/{})",
                SocketD.protocolVersion(),
                SocketD.version(),
                FolkMQ.versionName());
    }


    private void startLocalServerMode(MqWatcherSnapshotPlus snapshotPlus) throws Exception {
        //通讯架构
        String schema = Solon.cfg().get(MqConfigNames.folkmq_schema);

        //服务端（鉴权为可选。不添加则不鉴权）
        localServer = FolkMQ.createServer(schema)
                .config(c -> {
                    c.serialSend(true)
                            .maxMemoryRatio(0.8F)
                            .streamTimeout(MqServerConfig.streamTimeout)
                            .ioThreads(MqServerConfig.ioThreads)
                            .codecThreads(MqServerConfig.codecThreads)
                            .exchangeThreads(MqServerConfig.exchangeThreads);

                    EventBus.publish(c);
                })
                .addAccessAll(MqServerConfig.getAccessMap());

        if (MqServerConfig.saveEnable) {
            localServer.watcher(snapshotPlus);
        }

        localServer.start(Solon.cfg().serverPort() + 10000);

        addApiEvent(localServer.getServerInternal());

        //加入容器
        appContext.wrapAndPut(MqBorkerInternal.class, localServer.getServerInternal());

        log.info("FlokMQ local server started!");
    }

    private void startBrokerSession(String brokerServers, MqWatcherSnapshotPlus snapshotPlus) throws Exception {
        brokerServiceListener = new MqBorkerListener(true);

        //允许控制台获取队列看板
        brokerServiceListener.doOn(MqConstants.ADMIN_VIEW_QUEUE, (s, m) -> {
            if (m.isRequest() || m.isSubscribe()) {
                String json = ONode.stringify(ViewUtils.queueView(brokerServiceListener));
                s.replyEnd(m, new StringEntity(json));
            }
        });

        //允许控制台获取服务信息（暂时只看内存）
        brokerServiceListener.doOn(MqConstants.ADMIN_VIEW_INSTANCE, (s, m) -> {
            if (m.isRequest() || m.isSubscribe()) {
                ServerInfoVo infoVo = new ServerInfoVo();
                infoVo.memoryRatio = MemoryUtils.getUseMemoryRatio();
                String json = ONode.stringify(infoVo);
                s.replyEnd(m, new StringEntity(json));
            }
        });

        //允许控制台强制派发
        brokerServiceListener.doOn(MqConstants.ADMIN_QUEUE_FORCE_DISTRIBUTE, (s, m) -> {
            String topic = m.meta(MqConstants.MQ_META_TOPIC);
            String consumerGroup = m.meta(MqConstants.MQ_META_CONSUMER_GROUP);

            queueForceService.forceDistribute(brokerServiceListener, topic, consumerGroup, false);
        });

        //允许控制台强制删除
        brokerServiceListener.doOn(MqConstants.ADMIN_QUEUE_FORCE_DELETE, (s, m) -> {
            String topic = m.meta(MqConstants.MQ_META_TOPIC);
            String consumerGroup = m.meta(MqConstants.MQ_META_CONSUMER_GROUP);

            queueForceService.forceDelete(brokerServiceListener, topic, consumerGroup, false);
        });

        //允许控制台强制清空
        brokerServiceListener.doOn(MqConstants.ADMIN_QUEUE_FORCE_CLEAR, (s, m) -> {
            String topic = m.meta(MqConstants.MQ_META_TOPIC);
            String consumerGroup = m.meta(MqConstants.MQ_META_CONSUMER_GROUP);

            queueForceService.forceClear(brokerServiceListener, topic, consumerGroup, false);
        });

        addApiEvent(brokerServiceListener);

        //快照
        if (MqServerConfig.saveEnable) {
            brokerServiceListener.watcher(snapshotPlus);
        }

        List<String> serverUrls = new ArrayList<>();

        //同时支持：Broker 和 Multi-Broker
        for (String url : brokerServers.split(",")) {
            url = url.trim().replace("folkmq://", "sd:tcp://");

            if (Utils.isEmpty(url)) {
                continue;
            }

            //确保有 @参数（外部可不加）
            if (url.contains("@=") == false) {
                if (url.contains("?")) {
                    url = url + "&@=" + MqConstants.BROKER_AT_SERVER;
                } else {
                    url = url + "?@=" + MqConstants.BROKER_AT_SERVER;
                }
            }

            //添加自己的主端口
            url = url + "&port=" + Solon.cfg().serverPort();

            serverUrls.add(url);
        }

        brokerSession = (ClusterClientSession) SocketD.createClusterClient(serverUrls)
                .config(c -> {
                    c.metaPut(MqConstants.FOLKMQ_VERSION, FolkMQ.versionCodeAsString())
                            .heartbeatInterval(6_000)
                            .serialSend(true)
                            .maxMemoryRatio(0.8F)
                            .ioThreads(MqServerConfig.ioThreads)
                            .codecThreads(MqServerConfig.codecThreads)
                            .exchangeThreads(MqServerConfig.exchangeThreads);
                    EventBus.publish(c);
                })
                .listen(brokerServiceListener)
                .open();


        //启动时恢复快照
        brokerServiceListener.start(null);

        //加入容器
        appContext.wrapAndPut(MqBorkerInternal.class, brokerServiceListener);

        log.info("FlokMQ broker service started!");
    }

    @Override
    public void prestop() throws Throwable {
        if (localServer != null) {
            localServer.prestop();
//            for (Session s1 : localServer.getServerInternal().getSessionAll()) {
//                RunUtils.runAndTry(s1::closeStarting);
//            }
        }

        if (brokerSession != null) {
            brokerSession.preclose();
//            for (ClientSession s1 : brokerSession.getSessionAll()) {
//                RunUtils.runAndTry(s1::closeStarting);
//            }
        }


        log.info("Server:main: folkmq-server: Stop starting (SOCKET.D/{}-{}, folkmq/{})",
                SocketD.protocolVersion(),
                SocketD.version(),
                FolkMQ.versionName());
    }

    @Override
    public void stop() throws Throwable {
        if (localServer != null) {
            //停止时会触发快照
            localServer.stop();
        }

        if (brokerSession != null) {
            brokerSession.close();
            //停止时会触发快照
            brokerServiceListener.stop(null);
        }

        log.info("Server:main: folkmq-server: Has Stopped (SOCKET.D/{}-{}, folkmq/{})",
                SocketD.protocolVersion(),
                SocketD.version(),
                FolkMQ.versionName());
    }

    private void addApiEvent(MqBorkerInternal serviceInternal) {
        FolkmqApiHandler handler = new FolkmqApiHandler(queueForceService, (MqBorkerListener) serviceInternal);
        serviceInternal.doOnEvent(MqConstants.MQ_API, handler);
    }
}