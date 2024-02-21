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
    private boolean transaction;
    private int qos = 1;

    public MqMessage(String content) {
        this.tid = StrUtils.guid();
        this.content = content;
    }

    /**
     * 事务ID
     */
    @Override
    public String getTid() {
        return tid;
    }

    /**
     * 内容
     */
    @Override
    public String getContent() {
        return content;
    }

    /**
     * 定时派发
     */
    public Date getScheduled() {
        return scheduled;
    }

    /**
     * 过期时间
     */
    @Override
    public Date getExpiration() {
        return expiration;
    }

    /**
     * 是否事务
     */
    public boolean isTransaction() {
        return transaction;
    }

    /**
     * 是否顺序
     */
    @Override
    public boolean isSequence() {
        return sequence;
    }

    /**
     * 质量等级（0 或 1）
     */
    public int getQos() {
        return qos;
    }

    /**
     * 定时派发
     */
    public MqMessage scheduled(Date scheduled) {
        this.scheduled = scheduled;
        return this;
    }

    /**
     * 过期时间
     */
    public MqMessage expiration(Date expiration) {
        this.expiration = expiration;
        return this;
    }

    /**
     * 是否事务（内部接口）
     */
    protected MqMessage internalTransaction(boolean transaction) {
        this.transaction = transaction;
        return this;
    }

    /**
     * 是否顺序
     */
    public MqMessage sequence(boolean sequence) {
        this.sequence = sequence;
        return this;
    }

    /**
     * 质量等级（0 或 1）
     */
    public MqMessage qos(int qos) {
        this.qos = qos;
        return this;
    }
}
