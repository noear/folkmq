package org.noear.folkmq.server.pro.admin.model;

import java.io.Serializable;

/**
 * @author noear
 * @since 1.0
 */
public class QueueVo implements Serializable {
    public String queue ="";

    public int sessionCount;
    public int messageCount;

    public long messageDelayedCount1;
    public long messageDelayedCount2;
    public long messageDelayedCount3;
    public long messageDelayedCount4;
    public long messageDelayedCount5;
    public long messageDelayedCount6;
    public long messageDelayedCount7;
    public long messageDelayedCount8;

    public void setSessionCount(int sessionCount) {
        this.sessionCount = sessionCount;
    }

    public String getQueue() {
        return queue;
    }

    public int getSessionCount() {
        return sessionCount;
    }
    public int getMessageCount() {
        return messageCount;
    }

    public long getMessageDelayedCount1() {
        return messageDelayedCount1;
    }

    public long getMessageDelayedCount2() {
        return messageDelayedCount2;
    }

    public long getMessageDelayedCount3() {
        return messageDelayedCount3;
    }

    public long getMessageDelayedCount4() {
        return messageDelayedCount4;
    }

    public long getMessageDelayedCount5() {
        return messageDelayedCount5;
    }

    public long getMessageDelayedCount6() {
        return messageDelayedCount6;
    }

    public long getMessageDelayedCount7() {
        return messageDelayedCount7;
    }

    public long getMessageDelayedCount8() {
        return messageDelayedCount8;
    }
}
