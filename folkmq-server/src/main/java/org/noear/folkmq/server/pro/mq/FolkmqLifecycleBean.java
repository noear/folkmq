package org.noear.folkmq.server.pro.mq;

import org.noear.folkmq.FolkMQ;
import org.noear.folkmq.common.MqConstants;
import org.noear.folkmq.server.MqServer;
import org.noear.folkmq.server.MqServiceInternal;
import org.noear.folkmq.server.MqServiceListener;
import org.noear.folkmq.server.pro.Config;
import org.noear.folkmq.server.pro.MqWatcherSnapshotPlus;
import org.noear.folkmq.server.pro.admin.dso.QueueForceService;
import org.noear.folkmq.server.pro.admin.dso.ViewUtils;
import org.noear.folkmq.server.pro.common.ConfigNames;
import org.noear.snack.ONode;
import org.noear.socketd.SocketD;
import org.noear.socketd.cluster.ClusterClientSession;
import org.noear.socketd.transport.client.ClientSession;
import org.noear.socketd.transport.core.Session;
import org.noear.socketd.transport.core.entity.StringEntity;
import org.noear.socketd.utils.RunUtils;
import org.noear.socketd.utils.StrUtils;
import org.noear.solon.Solon;
import org.noear.solon.Utils;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.core.AppContext;
import org.noear.solon.core.bean.LifecycleBean;
import org.noear.solon.core.event.AppPrestopEndEvent;
import org.noear.solon.core.event.EventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author noear
 * @since 1.0
 */
@Component
public class FolkmqLifecycleBean implements LifecycleBean , EventListener<AppPrestopEndEvent> {
    private static final Logger log = LoggerFactory.getLogger(FolkmqLifecycleBean.class);

    @Inject
    private AppContext appContext;

    @Inject
    private QueueForceService queueForceService;

    private MqServer localServer;

    private MqServiceListener brokerServiceListener;
    private ClusterClientSession brokerSession;
    private MqWatcherSnapshotPlus snapshotPlus;
    private boolean saveEnable;

    @Override
    public void start() throws Throwable {
        String brokerServer = Solon.cfg().get(ConfigNames.folkmq_broker);

        saveEnable = Solon.cfg().getBool(ConfigNames.folkmq_snapshot_enable, true);

        long save900 = Solon.cfg().getLong(ConfigNames.folkmq_snapshot_save900, 0);
        long save300 = Solon.cfg().getLong(ConfigNames.folkmq_snapshot_save300, 0);
        long save100 = Solon.cfg().getLong(ConfigNames.folkmq_snapshot_save100, 0);

        //初始化快照持久化
        snapshotPlus = new MqWatcherSnapshotPlus();
        snapshotPlus.save900Condition(save900);
        snapshotPlus.save300Condition(save300);
        snapshotPlus.save100Condition(save100);

        appContext.wrapAndPut(MqWatcherSnapshotPlus.class, snapshotPlus);

        if (Utils.isEmpty(brokerServer)) {
            Config.isStandalone = true;
            startLocalServerMode(snapshotPlus);
        } else {
            Config.isStandalone = false;
            startBrokerSession(brokerServer, snapshotPlus);
        }

        log.info("Server:main: folkmq-server: Started (SOCKET.D/{}-{}, folkmq/{})",
                SocketD.protocolVersion(),
                SocketD.version(),
                FolkMQ.version());
    }

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

    private void startLocalServerMode(MqWatcherSnapshotPlus snapshotPlus) throws Exception {
        //服务端（鉴权为可选。不添加则不鉴权）
        localServer = FolkMQ.createServer()
                .config(c -> c.coreThreads(1).maxThreads(1).sequenceMode(true))
                .addAccessAll(getAccessMap());

        if (saveEnable) {
            localServer.watcher(snapshotPlus);
        }

        localServer.start(Solon.cfg().serverPort() + 10000);

        addApiEvent(localServer.getServerInternal());

        //加入容器
        appContext.wrapAndPut(MqServiceInternal.class, localServer.getServerInternal());

        log.info("FlokMQ local server started!");
    }

    private void startBrokerSession(String brokerServers, MqWatcherSnapshotPlus snapshotPlus) throws Exception {
        brokerServiceListener = new MqServiceListener(true);

        //允许控制台获取队列看板
        brokerServiceListener.doOn(MqConstants.ADMIN_VIEW_QUEUE, (s, m) -> {
            if (m.isRequest() || m.isSubscribe()) {
                String json = ONode.stringify(ViewUtils.queueView(brokerServiceListener));
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
        if (saveEnable) {
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
                .config(c -> c.coreThreads(1).maxThreads(1))
                .listen(brokerServiceListener)
                .open();


        //启动时恢复快照
        brokerServiceListener.start(null);

        //加入容器
        appContext.wrapAndPut(MqServiceInternal.class, brokerServiceListener);

        log.info("FlokMQ broker service started!");
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
    }

    @Override
    public void onEvent(AppPrestopEndEvent appPrestopEndEvent) throws Throwable {
        if (localServer != null) {
            for (Session s1 : localServer.getServerInternal().getSessionAll()) {
                RunUtils.runAndTry(s1::closeStarting);
            }
        }

        if (brokerSession != null) {
            for (ClientSession s1 : brokerSession.getSessionAll()) {
                if (s1 instanceof Session) {
                    RunUtils.runAndTry(((Session) s1)::closeStarting);
                }
            }
        }
    }

    private void addApiEvent(MqServiceInternal serviceInternal) {
        FolkmqApiHandler handler = new FolkmqApiHandler(queueForceService, (MqServiceListener) serviceInternal);
        serviceInternal.doOnEvent(MqConstants.MQ_API, handler);
    }
}
