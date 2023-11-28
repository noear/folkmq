package org.noear.folkmq.server.pro.mq;

import org.noear.folkmq.FolkMQ;
import org.noear.folkmq.server.MqServer;
import org.noear.folkmq.server.MqServerInternal;
import org.noear.folkmq.server.pro.MqWatcherSnapshotPlus;
import org.noear.solon.Solon;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.core.AppContext;
import org.noear.solon.core.bean.LifecycleBean;
import org.noear.solon.core.util.RunUtil;

import java.util.concurrent.ScheduledFuture;

/**
 * @author noear
 * @since 1.0
 */
@Component
public class FolkmqLifecycleBean implements LifecycleBean {
    @Inject
    private AppContext appContext;

    private MqServer server;
    private ScheduledFuture<?> snapshotFuture;

    @Override
    public void start() throws Throwable {
        long save900 = Solon.cfg().getLong("folkmq.snapshot.save900", 0);
        long save300 = Solon.cfg().getLong("folkmq.snapshot.save300", 0);
        long save60 = Solon.cfg().getLong("folkmq.snapshot.save60", 0);

        //初始化快照持久化
        MqWatcherSnapshotPlus snapshotPlus = new MqWatcherSnapshotPlus();
        snapshotPlus.save900Condition(save900);
        snapshotPlus.save300Condition(save300);
        snapshotPlus.save60Condition(save60);

        //服务端（鉴权为可选。不添加则不鉴权）
        server = FolkMQ.createServer()
                .addAccessAll(Solon.cfg().getMap("folkmq.access."))
                .watcher(snapshotPlus)
                .start(Solon.cfg().serverPort() + 10000);
        //加入容器
        appContext.wrapAndPut(MqServerInternal.class, server);
    }

    @Override
    public void stop() throws Throwable {
        if (server != null) {
            //停止时会触发快照
            server.stop();
        }

        if (snapshotFuture != null) {
            snapshotFuture.cancel(false);
        }
    }
}
