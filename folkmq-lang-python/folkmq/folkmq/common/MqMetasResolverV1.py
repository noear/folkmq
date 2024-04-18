from socketd.transport.core import Entity
from socketd.transport.core.Message import Message
from socketd.transport.core.entity.EntityDefault import EntityDefault

from folkmq.client.MqMessage import MqMessage
from folkmq.common.MqMetasResolver import MqMetasResolver


class MqMetasResolverV1(MqMetasResolver):
    def version(self) -> int:
        return 1

    def getSender(m: Entity) -> str:
        pass

    def getKey(m: Entity) -> str:
        pass

    def getTag(m: Entity) -> str:
        pass

    def getTopic(m: Entity) -> str:
        pass

    def getConsumerGroup(m: Entity) -> str:
        pass

    def setConsumerGroup(m: Entity, consumerGroup: str):
        pass

    def getQos(m: Entity) -> int:
        pass

    def getTimes(m: Entity) -> int:
        pass

    def setTimes(m: Entity, times: int):
        pass

    def getExpiration(m: Entity) -> int:
        pass

    def setExpiration(m: Entity, expiration: int):
        pass

    def getScheduled(m: Entity) -> int:
        pass

    def setScheduled(m: Entity, scheduled: int):
        pass

    def isSequence(m: Entity) -> bool:
        pass

    def isTransaction(m: Entity) -> bool:
        pass

    def setTransaction(m: Entity, isTransaction: bool):
        pass

    def publishEntityBuild(topic: str, message: MqMessage) -> EntityDefault:
        pass

    def routingMessageBuild(topic: str, message: MqMessage) -> Message:
        pass