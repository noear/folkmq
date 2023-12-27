package org.noear.folkmq.server.pro.mq;

import org.noear.folkmq.FolkMQ;
import org.noear.folkmq.common.MqConstants;
import org.noear.folkmq.server.MqServer;
import org.noear.folkmq.server.MqServiceInternal;
import org.noear.folkmq.server.MqServiceListener;
import org.noear.folkmq.server.pro.MqWatcherSnapshotPlus;
import org.noear.folkmq.server.pro.admin.dso.ViewUtils;
import org.noear.folkmq.server.pro.common.ConfigNames;
import org.noear.snack.ONode;
import org.noear.socketd.SocketD;
import org.noear.socketd.transport.client.ClientSession;
import org.noear.socketd.transport.core.entity.StringEntity;
import org.noear.solon.Solon;
import org.noear.solon.Utils;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.core.AppContext;
import org.noear.solon.core.bean.LifecycleBean;
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

    @Inject
    private AppContext appContext;

    private MqServer localServer;

    private MqServiceListener brokerServiceListener;
    private ClientSession brokerSession;
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
            startLocalServerMode(snapshotPlus);
        } else {
            startBrokerSession(brokerServer, snapshotPlus);
        }

        log.info("Server:main: folkmq-server: Started (SOCKET.D/{}-{}, folkmq/{})",
                SocketD.protocolVersion(),
                SocketD.version(),
                FolkMQ.version());
    }

    private void startLocalServerMode(MqWatcherSnapshotPlus snapshotPlus) throws Exception {
        //服务端（鉴权为可选。不添加则不鉴权）
        localServer = FolkMQ.createServer()
                .config(c -> c.coreThreads(2).maxThreads(4))
                .addAccessAll(Solon.cfg().getMap(ConfigNames.folkmq_access_x));

        if (saveEnable) {
            localServer.watcher(snapshotPlus);
        }

        localServer.start(Solon.cfg().serverPort() + 10000);

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

        brokerSession = SocketD.createClusterClient(serverUrls)
                .config(c -> c.coreThreads(2).maxThreads(4))
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
}
