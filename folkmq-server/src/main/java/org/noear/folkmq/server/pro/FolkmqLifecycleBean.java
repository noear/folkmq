package org.noear.folkmq.server.pro;

import org.noear.folkmq.server.MqServer;
import org.noear.folkmq.server.MqServerDefault;
import org.noear.socketd.utils.RunUtils;
import org.noear.solon.Solon;
import org.noear.solon.annotation.Component;
import org.noear.solon.core.bean.LifecycleBean;

import java.util.concurrent.ScheduledFuture;

/**
 * @author noear
 * @since 1.0
 */
@Component
public class FolkmqLifecycleBean implements LifecycleBean {
    private MqServer server;
    private ScheduledFuture<?> snapshotFuture;

    @Override
    public void start() throws Throwable {
        //服务端（鉴权为可选。不添加则不鉴权）
        server = new MqServerDefault()
                .addAccessAll(Solon.cfg().getMap("folkmq.access."))
                .persistent(new MqPersistentSnapshot())
                .start(Solon.cfg().serverPort());

        //添加定时快照
        snapshotFuture = RunUtils.delayAndRepeat(server::save, 1000 * 30);
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
