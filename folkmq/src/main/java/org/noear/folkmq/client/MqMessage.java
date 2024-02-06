package org.noear.folkmq.client;

import org.noear.socketd.utils.StrUtils;

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
    private Date expiration;
    private boolean sequence;
    private int qos = 1;

    public MqMessage(String content){
        this.tid = StrUtils.guid();
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

    @Override
    public Date getExpiration() {
        return expiration;
    }

    public boolean getSequence() {
        return sequence;
    }

    public int getQos() {
        return qos;
    }

    public MqMessage scheduled(Date scheduled) {
        this.scheduled = scheduled;
        return this;
    }

    public MqMessage expiration(Date expiration){
        this.expiration = expiration;
        return this;
    }

    public MqMessage sequence(boolean sequence) {
        this.sequence = sequence;
        return this;
    }

    public MqMessage qos(int qos) {
        this.qos = qos;
        return this;
    }
}
