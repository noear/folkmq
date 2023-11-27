package org.noear.folkmq.server.pro.admin.model;

/**
 * @author noear
 * @since 1.0
 */
public class QueueVo {
    private String queue;
    private int messageCount;
    private int sessionCount;

    public void setQueue(String queue) {
        this.queue = queue;
    }

    public void setMessageCount(int messageCount) {
        this.messageCount = messageCount;
    }

    public void setSessionCount(int sessionCount) {
        this.sessionCount = sessionCount;
    }

    public String getQueue() {
        return queue;
    }

    public int getMessageCount() {
        return messageCount;
    }

    public int getSessionCount() {
        return sessionCount;
    }
}
