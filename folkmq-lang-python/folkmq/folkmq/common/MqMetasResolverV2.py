from socketd.transport.core import Entity
from socketd.transport.core.Message import Message
from socketd.transport.core.entity.EntityDefault import EntityDefault

from folkmq.client.MqMessage import MqMessage
from folkmq.common.MqMetasResolver import MqMetasResolver

class MqMetasResolverV2(MqMetasResolver):
    def version(self) -> int:
        return 2

    def getSender(self, m: Entity) -> str:
        pass

    def getKey(self, m: Entity) -> str:
        pass

    def getTag(self, m: Entity) -> str:
        pass

    def getTopic(self, m: Entity) -> str:
        pass

    def getConsumerGroup(self, m: Entity) -> str:
        pass

    def setConsumerGroup(self, m: Entity, consumerGroup: str):
        pass

    def getQos(self, m: Entity) -> int:
        pass

    def getTimes(self, m: Entity) -> int:
        pass

    def setTimes(self, m: Entity, times: int):
        pass

    def getExpiration(self, m: Entity) -> int:
        pass

    def setExpiration(self, m: Entity, expiration: int):
        pass

    def getScheduled(self, m: Entity) -> int:
        pass

    def setScheduled(self, m: Entity, scheduled: int):
        pass

    def isSequence(self, m: Entity) -> bool:
        pass

    def isTransaction(self, m: Entity) -> bool:
        pass

    def setTransaction(self, m: Entity, isTransaction: bool):
        pass

    def publishEntityBuild(self, topic: str, message: MqMessage) -> EntityDefault:
        pass

    def routingMessageBuild(self, topic: str, message: MqMessage) -> Message:
        pass