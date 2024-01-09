import {SocketD} from "@noear/socket.d";
import {StringEntity} from "@noear/socket.d/transport/core/Entity";
import {Message, MessageBuilder} from "@noear/socket.d/transport/core/Message";
import {Flags} from "@noear/socket.d/transport/core/Constants";
import {StrUtils} from "@noear/socket.d/utils/StrUtils";
import {IMqMessage} from "../client/IMqMessage";
import {MqConstants} from "./MqConstants";

/**
 * 消息工具类
 *
 * @author noear
 * @see 1.0
 */
export class MqUtils {
    /**
     * 发布实体构建
     *
     * @param topic   主题
     * @param message 消息
     */
    static publishEntityBuild(topic: string, message: IMqMessage): StringEntity {
        //构建消息实体
        const entity = SocketD.newEntity(message.getContent());
        entity.metaPut(MqConstants.MQ_META_TID, message.getTid());
        entity.metaPut(MqConstants.MQ_META_TOPIC, topic);
        entity.metaPut(MqConstants.MQ_META_QOS, (message.getQos() == 0 ? "0" : "1"));
        if (message.getScheduled() == null) {
            entity.metaPut(MqConstants.MQ_META_SCHEDULED, "0");
        } else {
            entity.metaPut(MqConstants.MQ_META_SCHEDULED, message.getScheduled()!.getTime().toString());
        }
        entity.metaPut("@", MqConstants.BROKER_AT_SERVER);

        return entity;
    }

    /**
     * 路由消息构建
     *
     * @param topic   主题
     * @param message 消息
     */
    static routingMessageBuild(topic: string, message: IMqMessage): Message {
        let entity = MqUtils.publishEntityBuild(topic, message)
            .metaPut("@", MqConstants.BROKER_AT_SERVER);

        let messageDefault = new MessageBuilder()
            .flag(Flags.Message)
            .sid(StrUtils.guid())
            .event(MqConstants.MQ_EVENT_PUBLISH)
            .entity(entity)
            .build();

        return messageDefault;
    }
}