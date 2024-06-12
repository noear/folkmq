import {Entity} from "@noear/socket.d/transport/core/Entity";
import {EntityDefault} from "@noear/socket.d/transport/core/entity/EntityDefault";
import {Message, MessageBuilder} from "@noear/socket.d/transport/core/Message";
import { MqMessage } from "../client/MqMessage";
import {MqMetasResolver} from "./MqMetasResolver";
import {SocketD} from "@noear/socket.d";
import {MqConstants} from "./MqConstants";
import {Flags} from "@noear/socket.d/transport/core/Flags";
import {StrUtils} from "@noear/socket.d/utils/StrUtils";
import {MqMetasV2} from "./MqMetasV2";
import {MqMetasV1} from "./MqMetasV1";

export class MqMetasResolverV1 implements MqMetasResolver {
    version(): number {
        return 1;
    }

    getSender(m: Entity): string {
        return m.metaOrDefault(MqMetasV2.MQ_META_SENDER, "");
    }

    getKey(m: Entity): string {
        return m.metaOrDefault(MqMetasV1.MQ_META_KEY, "");
    }

    getTag(m: Entity): string {
        return m.metaOrDefault(MqMetasV2.MQ_META_TAG, "");
    }

    getTopic(m: Entity): string {
        return m.metaOrDefault(MqMetasV1.MQ_META_TOPIC, "");
    }

    getConsumerGroup(m: Entity): string {
        return m.metaOrDefault(MqMetasV1.MQ_META_CONSUMER_GROUP, "");
    }

    setConsumerGroup(m: Entity, consumerGroup: string) {
        m.putMeta(MqMetasV1.MQ_META_CONSUMER_GROUP, consumerGroup);
    }

    getQos(m: Entity): number {
        return "0" == m.meta(MqMetasV1.MQ_META_QOS) ? 0 : 1;
    }

    getTimes(m: Entity): number {
        return parseInt(m.metaOrDefault(MqMetasV1.MQ_META_TIMES, "0"));
    }

    setTimes(m: Entity, times: number) {
        m.putMeta(MqMetasV1.MQ_META_TIMES, times.toString());
    }

    getExpiration(m: Entity): number {
        return parseInt(m.metaOrDefault(MqMetasV1.MQ_META_EXPIRATION, "0"));
    }

    setExpiration(m: Entity, expiration: number) {
        if (expiration == null) {
            m.delMeta(MqMetasV1.MQ_META_EXPIRATION);
        } else {
            m.putMeta(MqMetasV1.MQ_META_EXPIRATION, expiration.toString());
        }
    }

    getScheduled(m: Entity): number {
        return parseInt(m.metaOrDefault(MqMetasV1.MQ_META_SCHEDULED, "0"));
    }

    setScheduled(m: Entity, scheduled: number) {
        m.putMeta(MqMetasV1.MQ_META_SCHEDULED, scheduled.toString());
    }

    isSequence(m: Entity): boolean {
        return "1" == (m.metaOrDefault(MqMetasV1.MQ_META_SEQUENCE, "0"));
    }

    isBroadcast(m: Entity): boolean {
        return "1" == (m.meta(MqMetasV2.MQ_META_BROADCAST));
    }

    isTransaction(m: Entity): boolean {
        return "1" == (m.meta(MqMetasV2.MQ_META_TRANSACTION));
    }

    setTransaction(m: Entity, isTransaction: boolean) {
        m.putMeta(MqMetasV2.MQ_META_TRANSACTION, (isTransaction ? "1" : "0"));
    }

    publishEntityBuild(topic: string, message: MqMessage): EntityDefault {
        //构建消息实体
        const entity = SocketD.newEntity(message.getBody());

        entity.metaPut(MqMetasV1.MQ_META_KEY, message.getKey());
        entity.metaPut(MqMetasV1.MQ_META_TOPIC, topic);
        entity.metaPut(MqMetasV1.MQ_META_QOS, (message.getQos() == 0 ? "0" : "1"));

        //标签
        if (message.getTag()) {
            entity.metaPut(MqMetasV2.MQ_META_TAG, message.getTag());
        }

        //定时派发
        if (message.getScheduled() == null) {
            entity.metaPut(MqMetasV1.MQ_META_SCHEDULED, "0");
        } else {
            entity.metaPut(MqMetasV1.MQ_META_SCHEDULED, message.getScheduled()!.getTime().toString());
        }

        //过期时间
        if (message.getExpiration() != null) {
            entity.metaPut(MqMetasV1.MQ_META_EXPIRATION, message.getExpiration()!.getTime().toString());
        }

        if (message.isTransaction()) {
            entity.metaPut(MqMetasV2.MQ_META_TRANSACTION, "1");
        }

        if (message.getSender()) {
            entity.metaPut(MqMetasV2.MQ_META_SENDER, message.getSender());
        }

        if(message.isBroadcast()){
            entity.metaPut(MqMetasV2.MQ_META_BROADCAST, "1");
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

        //用户属性
        message.getAttrMap().forEach((value, key, map) => {
            entity.putMeta(MqConstants.MQ_ATTR_PREFIX + key, value);
        })

        return entity;
    }

    routingMessageBuild(topic: string, message: MqMessage): Message {
        let entity = this.publishEntityBuild(topic, message)
            .at(MqConstants.BROKER_AT_SERVER);

        let messageDefault = new MessageBuilder()
            .flag(Flags.Message)
            .sid(StrUtils.guid())
            .event(MqConstants.MQ_EVENT_PUBLISH)
            .entity(entity)
            .build();

        return messageDefault;
    }
}