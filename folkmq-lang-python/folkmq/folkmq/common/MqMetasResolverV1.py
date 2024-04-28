
from socketd import SocketD
from socketd.transport.core import Entity
from socketd.transport.core.Flags import Flags
from socketd.transport.core.Message import Message
from socketd.transport.core.entity.EntityDefault import EntityDefault
from socketd.transport.core.entity.MessageBuilder import MessageBuilder
from socketd.utils.StrUtils import StrUtils

from folkmq.client.MqMessage import MqMessage
from folkmq.common.MqConstants import MqConstants
from folkmq.common.MqMetasResolver import MqMetasResolver
from folkmq.common.MqMetasV1 import MqMetasV1
from folkmq.common.MqMetasV2 import MqMetasV2


class MqMetasResolverV1(MqMetasResolver):
    def version(self) -> int:
        return 1

    def getSender(self, m: Entity) -> str:
        return m.meta_or_default(MqMetasV2.MQ_META_SENDER, "");

    def getKey(self, m: Entity) -> str:
        return m.meta_or_default(MqMetasV1.MQ_META_KEY, "");

    def getTag(self, m: Entity) -> str:
        return m.meta_or_default(MqMetasV2.MQ_META_TAG, "");

    def getTopic(self, m: Entity) -> str:
        return m.meta_or_default(MqMetasV1.MQ_META_TOPIC, "");

    def getConsumerGroup(self, m: Entity) -> str:
        return m.meta_or_default(MqMetasV1.MQ_META_CONSUMER_GROUP, "");

    def setConsumerGroup(self, m: Entity, consumerGroup: str):
        m.put_meta(MqMetasV1.MQ_META_CONSUMER_GROUP, consumerGroup);

    def getQos(self, m: Entity) -> int:
        return 0 if "0" == m.meta(MqMetasV1.MQ_META_QOS) else 1;

    def getTimes(self, m: Entity) -> int:
        return int(m.meta_or_default(MqMetasV1.MQ_META_TIMES, "0"));

    def setTimes(self, m: Entity, times: int):
        m.put_meta(MqMetasV1.MQ_META_TIMES, times.toString());

    def getExpiration(self, m: Entity) -> float:
        return float(m.meta_or_default(MqMetasV1.MQ_META_EXPIRATION, "0"));

    def setExpiration(self, m: Entity, expiration: int|None):
        if expiration is None:
            m.delMeta(MqMetasV1.MQ_META_EXPIRATION)
        else:
            m.put_meta(MqMetasV1.MQ_META_EXPIRATION, expiration.toString())

    def getScheduled(self, m: Entity) -> int:
        return int(m.meta_or_default(MqMetasV1.MQ_META_SCHEDULED, "0"))

    def setScheduled(self, m: Entity, scheduled: int):
        m.put_meta(MqMetasV1.MQ_META_SCHEDULED, scheduled.toString())

    def isSequence(self, m: Entity) -> bool:
        return "1" == (m.meta_or_default(MqMetasV1.MQ_META_SEQUENCE, "0"))

    def isTransaction(self, m: Entity) -> bool:
        return "1" == (m.meta(MqMetasV2.MQ_META_TRANSACTION))

    def setTransaction(self, m: Entity, isTransaction: bool):
        m.put_meta(MqMetasV2.MQ_META_TRANSACTION, ( "1" if isTransaction else "0"))

    def publishEntityBuild(self, topic: str, message: MqMessage) -> EntityDefault:
        # 构建消息实体
        entity = EntityDefault().data_set(message.getBody())

        entity.meta_put(MqMetasV1.MQ_META_KEY, message.getKey())
        entity.meta_put(MqMetasV1.MQ_META_TOPIC, topic)
        entity.meta_put(MqMetasV1.MQ_META_QOS, ("0" if message.getQos() == 0 else "1"))

        # 标签
        if message.getTag():
            entity.meta_put(MqMetasV2.MQ_META_TAG, message.getTag())


        # 定时派发
        if message.getScheduled():
            entity.meta_put(MqMetasV1.MQ_META_SCHEDULED, str(message.getScheduled().getTime()))
        else:
            entity.meta_put(MqMetasV1.MQ_META_SCHEDULED, "0")

        # 过期时间
        if message.getExpiration() is not None:
            entity.meta_put(MqMetasV1.MQ_META_EXPIRATION, str(message.getExpiration().getTime()))


        if  message.isTransaction():
            entity.meta_put(MqMetasV2.MQ_META_TRANSACTION, "1")


        if message.getSender():
            entity.meta_put(MqMetasV2.MQ_META_SENDER, message.getSender())


        # 是否有序
        if  message.isSequence() or message.isTransaction():
            entity.at(MqConstants.BROKER_AT_SERVER_HASH)

        if message.isSequence():
            entity.meta_put(MqMetasV1.MQ_META_SEQUENCE, "1")
        else:
            entity.at(MqConstants.BROKER_AT_SERVER)

        # 用户属性
        #message.getAttrMap().forEach((value, key, map) = > {
        #    entity.put_meta(MqConstants.MQ_ATTR_PREFIX + key, value)
        #})

        return entity

    def routingMessageBuild(self, topic: str, message: MqMessage) -> Message:
        entity = self.publishEntityBuild(topic, message).at(MqConstants.BROKER_AT_SERVER)
        messageDefault =  MessageBuilder().flag(Flags.Message) \
                                .sid(StrUtils.guid()) \
                                .event(MqConstants.MQ_EVENT_PUBLISH) \
                                .entity(entity) \
                                .build()
        return messageDefault