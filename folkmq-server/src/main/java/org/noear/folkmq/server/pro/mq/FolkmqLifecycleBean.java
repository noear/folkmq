package org.noear.folkmq.server.pro.mq;

import org.noear.folkmq.FolkMQ;
import org.noear.folkmq.server.MqServer;
import org.noear.folkmq.server.MqServerInternal;
import org.noear.folkmq.server.pro.MqWatcherSnapshot;
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
        //服务端（鉴权为可选。不添加则不鉴权）
        server = FolkMQ.createServer()
                .addAccessAll(Solon.cfg().getMap("folkmq.access."))
                .watcher(new MqWatcherSnapshot())
                .start(Solon.cfg().serverPort() + 10000);

        //添加定时快照
        snapshotFuture = RunUtil.scheduleWithFixedDelay(server::save, 1000 * 30, 1000 * 30);

        //加入容器
        appContext.wrapAndPut(MqServerInternal.class, server);
    }

    @Override
    public void stop() throws Throwable {
        if (server != null) {
            server.stop();
        }

        if (snapshotFuture != null) {
            snapshotFuture.cancel(false);
        }
    }
}
