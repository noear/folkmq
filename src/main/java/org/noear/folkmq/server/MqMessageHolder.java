package org.noear.folkmq.server;

import org.noear.socketd.transport.core.Message;
import org.noear.socketd.transport.core.entity.EntityDefault;

import java.io.IOException;
import java.util.concurrent.ScheduledFuture;

/**
 * 消息持有人（为消息添加状态信息）
 *
 * @author noear
 * @since 1.0
 */
public class MqMessageHolder {
    //来源
    private transient Message from;
    //消息内容
    private EntityDefault content;
    //派发时间
    private long distributeTime;
    //派发次数
    private int distributeCount;

    //延时任务
    protected ScheduledFuture<?> delayedFuture;

    public MqMessageHolder(Message from, long distributeTime) {
        this.from = from;
        this.content = new EntityDefault().data(from.dataAsBytes()).metaMap(from.metaMap());
        this.distributeTime = distributeTime;
    }

    /**
     * 获取流Id
     */
    public String getSid() {
        return from.sid();
    }

    /**
     * 获取消息内容
     */
    public EntityDefault getContent() throws IOException {
        content.data().reset();
        return content;
    }

    /**
     * 获取派发时间（单位：毫秒）
     */
    public long getDistributeTime() {
        return distributeTime;
    }

    /**
     * 获取派发次数
     */
    public int getDistributeCount() {
        return distributeCount;
    }

    /**
     * 延后（生成下次派发时间）
     */
    public MqMessageHolder delayed() {
        distributeCount++;
        distributeTime = MqNextTime.getNextTime(this);
        return this;
    }
}
