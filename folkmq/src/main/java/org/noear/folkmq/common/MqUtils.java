package org.noear.folkmq.common;

import org.noear.folkmq.client.IMqMessage;
import org.noear.socketd.transport.core.Entity;
import org.noear.socketd.transport.core.Flags;
import org.noear.socketd.transport.core.Message;
import org.noear.socketd.transport.core.MessageInternal;
import org.noear.socketd.transport.core.entity.StringEntity;
import org.noear.socketd.transport.core.entity.MessageBuilder;
import org.noear.socketd.utils.StrUtils;

/**
 * 消息工具类
 *
 * @author noear
 * @see 1.0
 */
public class MqUtils {
    /**
     * 发布实体构建
     *
     * @param topic   主题
     * @param message 消息
     */
    public static StringEntity publishEntityBuild(String topic, IMqMessage message) {
        //构建消息实体
        StringEntity entity = new StringEntity(message.getContent());
        entity.metaPut(MqConstants.MQ_META_TID, message.getTid());
        entity.metaPut(MqConstants.MQ_META_TOPIC, topic);
        entity.metaPut(MqConstants.MQ_META_QOS, (message.getQos() == 0 ? "0" : "1"));
        if (message.getScheduled() == null) {
            entity.metaPut(MqConstants.MQ_META_SCHEDULED, "0");
        } else {
            entity.metaPut(MqConstants.MQ_META_SCHEDULED, String.valueOf(message.getScheduled().getTime()));
        }
        entity.at(MqConstants.BROKER_AT_SERVER);

        return entity;
    }

    /**
     * 路由消息构建
     *
     * @param topic   主题
     * @param message 消息
     */
    public static Message routingMessageBuild(String topic, IMqMessage message) {
        Entity entity = MqUtils.publishEntityBuild(topic, message)
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