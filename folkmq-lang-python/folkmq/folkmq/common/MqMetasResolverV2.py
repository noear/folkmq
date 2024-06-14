

from socketd.transport.core import Entity
from socketd.transport.core.EntityMetas import EntityMetas
from socketd.transport.core.Flags import Flags
from socketd.transport.core.Message import Message
from socketd.transport.core.entity.EntityDefault import EntityDefault
from socketd.transport.core.entity.MessageBuilder import MessageBuilder
from socketd.utils.StrUtils import StrUtils

from folkmq.client.MqMessage import MqMessage
from folkmq.common.MqConstants import MqConstants
from folkmq.common.MqMetasResolver import MqMetasResolver
from folkmq.common.MqMetasV2 import MqMetasV2


class MqMetasResolverV2(MqMetasResolver):
    def version(self) -> int:
        return 2

    def get_sender(self, m: Entity) -> str:
        return m.meta_or_default(MqMetasV2.MQ_META_SENDER, "")

    def get_key(self, m: Entity) -> str:
        return m.meta_or_default(MqMetasV2.MQ_META_KEY, "")

    def get_tag(self, m: Entity) -> str:
        return m.meta_or_default(MqMetasV2.MQ_META_TAG, "")

    def get_topic(self, m: Entity) -> str:
        return m.meta_or_default(MqMetasV2.MQ_META_TOPIC, "")

    def get_consumer_group(self, m: Entity) -> str:
        return m.meta_or_default(MqMetasV2.MQ_META_CONSUMER_GROUP, "")

    def set_consumer_group(self, m: Entity, consumerGroup: str):
        m.put_meta(MqMetasV2.MQ_META_CONSUMER_GROUP, consumerGroup)

    def get_qos(self, m: Entity) -> int:
        return  0 if "0" == (m.meta(MqMetasV2.MQ_META_QOS)) else 1

    def get_times(self, m: Entity) -> int:
        return int(m.meta_or_default(MqMetasV2.MQ_META_TIMES, "0"))

    def set_times(self, m: Entity, times: int):
        m.put_meta(MqMetasV2.MQ_META_TIMES, str(times))

    def get_expiration(self, m: Entity) -> float:
        return float(m.meta_or_default(MqMetasV2.MQ_META_EXPIRATION, "0"))

    def set_expiration(self, m: Entity, expiration: int):
        if  expiration is None:
            m.del_meta(MqMetasV2.MQ_META_EXPIRATION)
        else:
            m.put_meta(MqMetasV2.MQ_META_EXPIRATION, str(expiration))

    def get_scheduled(self, m: Entity) -> int:
        return int(m.meta_or_default(MqMetasV2.MQ_META_SCHEDULED, "0"))

    def set_scheduled(self, m: Entity, scheduled: int):
        m.put_meta(MqMetasV2.MQ_META_SCHEDULED, str(scheduled))

    def is_sequence(self, m: Entity) -> bool:
        return "1" == (m.meta(MqMetasV2.MQ_META_SEQUENCE))

    def is_broadcast(self, m: Entity) ->bool:
        return "1" == (m.meta(MqMetasV2.MQ_META_BROADCAST))

    def is_transaction(self, m: Entity) -> bool:
        return "1" == (m.meta(MqMetasV2.MQ_META_TRANSACTION))

    def set_transaction(self, m: Entity, isTransaction: bool):
        m.put_meta(MqMetasV2.MQ_META_TRANSACTION, ("1" if isTransaction else "0"))

    def publish_entity_build(self, topic: str, message: MqMessage) -> EntityDefault:
        #构建消息实体
        entity = EntityDefault().data_set(message.get_body())

        entity.meta_put(MqMetasV2.MQ_META_KEY, message.get_key())
        entity.meta_put(MqMetasV2.MQ_META_TOPIC, topic)
        entity.meta_put(MqMetasV2.MQ_META_QOS, ("0" if message.get_qos() == 0 else "1"))

        # 标签
        if message.get_tag():
            entity.meta_put(MqMetasV2.MQ_META_TAG, message.get_tag())

        #定时派发
        if message.getScheduled() is None:
            entity.meta_put(MqMetasV2.MQ_META_SCHEDULED, "0")
        else:
            entity.meta_put(MqMetasV2.MQ_META_SCHEDULED, str(message.getScheduled()))

        # 过期时间
        if message.get_expiration() is not None:
            entity.meta_put(MqMetasV2.MQ_META_EXPIRATION, str(message.get_expiration()))

        if message.is_transaction():
            entity.meta_put(MqMetasV2.MQ_META_TRANSACTION, "1")

        if message.get_sender():
            entity.meta_put(MqMetasV2.MQ_META_SENDER, message.get_sender())

        if message.is_broadcast():
            entity.meta_put(MqMetasV2.MQ_META_BROADCAST, "1")

        # 是否有序
        if message.is_sequence() or message.is_transaction():
            entity.at(MqConstants.PROXY_AT_BROKER_HASH)
            if message.is_sequence():
                entity.meta_put(MqMetasV2.MQ_META_SEQUENCE, "1");

                if message.is_transaction() == False and message.getSequenceSharding():
                    # 不是事务，并且有顺序分片
                    entity.meta_put(EntityMetas.META_X_HASH, message.getSequenceSharding())
        else:
            entity.at(MqConstants.PROXY_AT_BROKER)

        from folkmq.FolkMQ import FolkMQ
        entity.meta_put(MqMetasV2.MQ_META_VID, str(FolkMQ.version_code()))

        #用户属性
        for kv in message.get_attr_map():
            entity.put_meta(MqConstants.MQ_ATTR_PREFIX + kv[0], kv[1])

        return entity

    def routing_message_build(self, topic: str, message: MqMessage) -> Message:
        entity = self.publish_entity_build(topic, message)
        messageDefault = (MessageBuilder()
                          .flag(Flags.Message)
                          .sid(StrUtils.guid())
                          .event(MqConstants.MQ_EVENT_PUBLISH)
                          .entity(entity)
                          .build())
        return messageDefault