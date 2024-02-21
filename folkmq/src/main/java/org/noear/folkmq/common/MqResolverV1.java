package org.noear.folkmq.common;

import org.noear.folkmq.client.MqMessage;
import org.noear.socketd.transport.core.Entity;
import org.noear.socketd.transport.core.Flags;
import org.noear.socketd.transport.core.Message;
import org.noear.socketd.transport.core.MessageInternal;
import org.noear.socketd.transport.core.entity.MessageBuilder;
import org.noear.socketd.transport.core.entity.StringEntity;
import org.noear.socketd.utils.StrUtils;

/**
 * 消息元信息分析器 v1
 *
 * @author noear
 * @see 1.2
 */
public class MqResolverV1 implements MqResolver {
    public String getTid(Message m) {
        return m.metaOrDefault(MqMetasV1.MQ_META_TID, "");
    }

    public String getTopic(Message m) {
        return m.metaOrDefault(MqMetasV1.MQ_META_TOPIC, "");
    }

    public String getConsumerGroup(Message m) {
        return m.metaOrDefault(MqMetasV1.MQ_META_CONSUMER_GROUP, "");
    }

    public void setConsumerGroup(Entity m, String consumerGroup){
        m.putMeta(MqMetasV1.MQ_META_CONSUMER_GROUP, consumerGroup);
    }

    public int getQos(Message m) {
        return "0".equals(m.meta(MqMetasV1.MQ_META_QOS)) ? 0 : 1;
    }

    public int getTimes(Message m) {
        return Integer.parseInt(m.metaOrDefault(MqMetasV1.MQ_META_TIMES, "0"));
    }

    public void setTimes(Entity m, int times){
        m.putMeta(MqMetasV1.MQ_META_TIMES, String.valueOf(times));
    }

    public long getExpiration(Message m) {
        return Long.parseLong(m.metaOrDefault(MqMetasV1.MQ_META_EXPIRATION, "0"));
    }

    public long getScheduled(Message m) {
        return Long.parseLong(m.metaOrDefault(MqMetasV1.MQ_META_SCHEDULED, "0"));
    }

    public void setScheduled(Entity m, long scheduled){
        m.putMeta(MqMetasV1.MQ_META_SCHEDULED, String.valueOf(scheduled));
    }

    public boolean isSequence(Message m) {
        return Integer.parseInt(m.metaOrDefault(MqMetasV1.MQ_META_SEQUENCE, "0")) == 1;
    }

    public boolean isTransaction(Message m) {
        return "1".equals(m.meta(MqMetasV2.MQ_META_TRANSACTION));
    }


    /**
     * 发布实体构建
     *
     * @param topic   主题
     * @param message 消息
     */
    public StringEntity publishEntityBuild(String topic, MqMessage message) {
        //构建消息实体
        StringEntity entity = new StringEntity(message.getContent());
        entity.metaPut(MqMetasV1.MQ_META_TID, message.getTid());
        entity.metaPut(MqMetasV1.MQ_META_TOPIC, topic);
        entity.metaPut(MqMetasV1.MQ_META_QOS, (message.getQos() == 0 ? "0" : "1"));

        //定时派发
        if (message.getScheduled() == null) {
            entity.metaPut(MqMetasV1.MQ_META_SCHEDULED, "0");
        } else {
            entity.metaPut(MqMetasV1.MQ_META_SCHEDULED, String.valueOf(message.getScheduled().getTime()));
        }

        //过期时间
        if (message.getExpiration() == null) {
            entity.metaPut(MqMetasV1.MQ_META_EXPIRATION, "0");
        } else {
            entity.metaPut(MqMetasV1.MQ_META_EXPIRATION, String.valueOf(message.getExpiration().getTime()));
        }

        if (message.isTransaction()) {
            entity.metaPut(MqMetasV2.MQ_META_TRANSACTION, "1");
            entity.metaPut(MqMetasV2.MQ_META_SENDER, message.getSender());
        }

        //是否有序
        if (message.isSequence() || message.isTransaction()) {
            entity.at(MqConstants.BROKER_AT_SERVER_HASH);

            if (message.isSequence()) {
                entity.metaPut(MqMetasV1.MQ_META_SEQUENCE, "1");
            }
        } else {
            entity.at(MqConstants.BROKER_AT_SERVER);
        }

        return entity;
    }

    /**
     * 路由消息构建
     *
     * @param topic   主题
     * @param message 消息
     */
    public Message routingMessageBuild(String topic, MqMessage message) {
        Entity entity = publishEntityBuild(topic, message)
                .at(MqConstants.BROKER_AT_SERVER);

        MessageInternal messageDefault = new MessageBuilder()
                .flag(Flags.Message)
                .sid(StrUtils.guid())
                .event(MqConstants.MQ_EVENT_PUBLISH)
                .entity(entity)
                .build();

        return messageDefault;
    }
}