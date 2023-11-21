package org.noear.folkmq.server;

import org.noear.socketd.transport.core.Entity;
import org.noear.socketd.transport.core.Message;
import org.noear.socketd.transport.core.entity.EntityDefault;
import org.noear.socketd.transport.core.entity.StringEntity;

import java.io.IOException;
import java.util.concurrent.ScheduledFuture;

/**
 * 消息持有人
 *
 * @author noear
 * @since 1.0
 */
public class MqMessageHolder {
    private Message from;
    private EntityDefault content;
    private long nextTime;
    private int times;

    protected ScheduledFuture<?> deferredFuture;

    public MqMessageHolder(Message from) {
        this.from = from;
        this.content = new StringEntity(from.dataAsString()).metaMap(from.metaMap());
    }

    /**
     * 获取消息内容
     * */
    public EntityDefault getContent() throws IOException {
        content.data().reset();
        return content;
    }

    /**
     * 获取下次派发时间（单位：毫秒）
     */
    public long getNextTime() {
        return nextTime;
    }

    /**
     * 获取派发次数
     */
    public int getTimes() {
        return times;
    }

    /**
     * 延后（生成下次派发时间）
     */
    public MqMessageHolder deferred() {
        times++;
        nextTime = MqNextTime.getNextTime(this);
        return this;
    }
}
