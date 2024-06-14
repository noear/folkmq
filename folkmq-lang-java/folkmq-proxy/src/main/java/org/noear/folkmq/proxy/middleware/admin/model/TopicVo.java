package org.noear.folkmq.proxy.middleware.admin.model;

/**
 * @author noear
 * @since 1.0
 */
public class TopicVo {
    private String topic;
    private int queueCount;
    private String queueList;

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public void setQueueCount(int queueCount) {
        this.queueCount = queueCount;
    }

    public void setQueueList(String queueList) {
        this.queueList = queueList;
    }

    public String getTopic() {
        return topic;
    }

    public int getQueueCount() {
        return queueCount;
    }

    public String getQueueList() {
        return queueList;
    }
}
