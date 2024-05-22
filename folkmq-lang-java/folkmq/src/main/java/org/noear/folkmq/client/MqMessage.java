package org.noear.folkmq.client;

import org.noear.folkmq.common.MqAssert;
import org.noear.socketd.utils.StrUtils;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 消息
 *
 * @author noear
 * @since 1.0
 */
public class MqMessage implements MqMessageBase {
    private final String key;
    private final byte[] body;

    private String sender;
    private String tag;
    private Date scheduled;
    private Date expiration;
    private boolean broadcast;
    private boolean sequence;
    private String sequenceSharding;
    private int qos = 1;
    private Map<String, String> attrMap = new HashMap<>();

    protected MqTransaction transaction;

    public MqMessage(String body) {
        this(body, null);
    }

    public MqMessage(byte[] body) {
        this(body, null);
    }

    public MqMessage(String body, String key) {
        this(body.getBytes(StandardCharsets.UTF_8), key);
    }

    public MqMessage(byte[] body, String key) {
        MqAssert.requireNonNull(body, "Param 'body' can't be null");

        if (StrUtils.isEmpty(key)) {
            this.key = StrUtils.guid();
        } else {
            this.key = key;
        }

        this.body = body;
    }

    /**
     * 发送者
     */
    @Override
    public String getSender() {
        return sender;
    }

    /**
     * 跟踪ID
     */
    @Override
    public String getKey() {
        return key;
    }

    /**
     * 标签
     */
    @Override
    public String getTag() {
        return tag;
    }

    @Override
    public byte[] getBody() {
        return body;
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
        return transaction != null;
    }

    /**
     * 是否广播
     */
    @Override
    public boolean isBroadcast() {
        return broadcast;
    }

    /**
     * 是否为顺序
     */
    @Override
    public boolean isSequence() {
        return sequence;
    }

    public String getSequenceSharding() {
        return sequenceSharding;
    }

    /**
     * 质量等级（0 或 1）
     */
    public int getQos() {
        return qos;
    }

    public MqMessage tag(String tag) {
        this.tag = tag;
        return this;
    }

    public MqMessage asJson() {
        attr("Content-Type", "application/json");
        return this;
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
     * 是否事务
     */
    public MqMessage transaction(MqTransaction transaction) {
        if (transaction != null) {
            this.transaction = transaction;
            transaction.binding(this);
        }

        return this;
    }

    /**
     * 是否广播
     */
    public MqMessage broadcast(boolean broadcast) {
        this.broadcast = broadcast;
        return this;
    }

    protected String getTmid() {
        if (transaction == null) {
            return null;
        } else {
            return transaction.tmid();
        }
    }

    protected MqMessage internalSender(String sender) {
        this.sender = sender;
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
     * 是否顺序
     *
     * @param sharding 虚拟分片（分片内顺序）
     */
    public MqMessage sequence(boolean sequence, String sharding) {
        this.sequence = sequence;
        this.sequenceSharding = (sequence ? sharding : null);
        return this;
    }

    /**
     * 质量等级（0 或 1）
     */
    public MqMessage qos(int qos) {
        this.qos = qos;
        return this;
    }

    /**
     * 属性获取
     */
    @Override
    public String getAttr(String name) {
        return attrMap.get(name);
    }

    /**
     * 获取属性字典
     */
    public Map<String, String> getAttrMap() {
        return Collections.unmodifiableMap(attrMap);
    }

    /**
     * 属性配置
     */
    public MqMessage attr(String name, String value) {
        MqAssert.assertMeta(name, "name");
        MqAssert.assertMeta(value, "value");

        attrMap.put(name, value);
        return this;
    }
}