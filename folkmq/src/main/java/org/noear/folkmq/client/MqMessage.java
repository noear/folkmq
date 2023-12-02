package org.noear.folkmq.client;

import org.noear.socketd.utils.Utils;

import java.util.Date;

/**
 * 消息
 *
 * @author noear
 * @since 1.0
 */
public class MqMessage implements IMqMessage {
    private String tid;
    private String content;
    private Date scheduled;
    private int qos = 1;

    public MqMessage(String content){
        this.tid = Utils.guid();
        this.content = content;
    }

    @Override
    public String getTid() {
        return tid;
    }

    public String getContent() {
        return content;
    }

    public Date getScheduled() {
        return scheduled;
    }

    public int getQos() {
        return qos;
    }



    public MqMessage scheduled(Date scheduled) {
        this.scheduled = scheduled;
        return this;
    }

    public MqMessage qos(int qos) {
        this.qos = qos;
        return this;
    }
}
