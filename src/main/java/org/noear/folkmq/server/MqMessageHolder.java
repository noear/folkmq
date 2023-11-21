package org.noear.folkmq.server;

import org.noear.socketd.transport.core.Message;

/**
 * 消息持有人
 *
 * @author noear
 * @since 1.0
 */
public class MqMessageHolder {
    private Message message;
    private long nextTime;
    private int times;

    public MqMessageHolder(Message message) {
        this.message = message;
    }

    /**
     * 获取消息
     */
    public Message getMessage() {
        return message;
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
