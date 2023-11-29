package org.noear.folkmq.server.pro.mq;

import org.noear.folkmq.FolkMQ;
import org.noear.folkmq.server.MqServer;
import org.noear.folkmq.server.MqServiceInternal;
import org.noear.folkmq.server.MqServiceListener;
import org.noear.folkmq.server.pro.MqWatcherSnapshotPlus;
import org.noear.socketd.SocketD;
import org.noear.socketd.transport.core.Session;
import org.noear.solon.Solon;
import org.noear.solon.Utils;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.core.AppContext;
import org.noear.solon.core.bean.LifecycleBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private Session brokerSession;

    @Override
    public void start() throws Throwable {
        String brokerServer = Solon.cfg().get("folkmq.broker");
        long save900 = Solon.cfg().getLong("folkmq.snapshot.save900", 0);
        long save300 = Solon.cfg().getLong("folkmq.snapshot.save300", 0);
        long save60 = Solon.cfg().getLong("folkmq.snapshot.save60", 0);

        //初始化快照持久化
        MqWatcherSnapshotPlus snapshotPlus = new MqWatcherSnapshotPlus();
        snapshotPlus.save900Condition(save900);
        snapshotPlus.save300Condition(save300);
        snapshotPlus.save60Condition(save60);

        if (Utils.isEmpty(brokerServer)) {
            //服务端（鉴权为可选。不添加则不鉴权）
            localServer = FolkMQ.createServer()
                    .addAccessAll(Solon.cfg().getMap("folkmq.access."))
                    .watcher(snapshotPlus)
                    .start(Solon.cfg().serverPort() + 10000);

            //加入容器
            appContext.wrapAndPut(MqServiceInternal.class, localServer.getServerInternal());

            log.info("FlokMQ local server started!");
        } else {
            MqServiceListener serviceListener = new MqServiceListener(true);
            serviceListener.watcher(snapshotPlus);

            brokerSession = SocketD.createClient(brokerServer)
                    .listen(serviceListener)
                    .open();

            //加入容器
            appContext.wrapAndPut(MqServiceInternal.class, serviceListener);

            log.info("FlokMQ broker service started!");
        }
    }

    @Override
    public void stop() throws Throwable {
        if (localServer != null) {
            //停止时会触发快照
            localServer.stop();
        }

        if (brokerSession != null) {
            brokerSession.close();
        }
    }
}
