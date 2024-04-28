from abc import abstractmethod
from datetime import datetime

from socketd.transport.core import Entity
from socketd.transport.core.Message import Message
from socketd.transport.core.Session import Session

from folkmq.client.MqMessage import MqMessageBase
from folkmq.common.MqConstants import MqConstants
from folkmq.common.MqTopicHelper import MqTopicHelper
from folkmq.common.MqUtils import MqUtils


class MqMessageReceived(MqMessageBase):
    # 主题
    @abstractmethod
    def getTopic(self) -> str:
        ...

    # 内容
    @abstractmethod
    def getBodyAsString(self) -> str:
        ...

    # 消费者组
    @abstractmethod
    def getConsumerGroup(self) -> str:
        ...

    # 已派发次数
    @abstractmethod
    def getTimes(self) -> int:
        ...

    # 回执
    @abstractmethod
    def acknowledge(self, isOk: bool):
        ...

    # 响应
    @abstractmethod
    def response(self, entity:Entity):
        ...

class MqMessageReceivedImpl(MqMessageReceived):
    def __init__(self, clientInternal:'MqClientInternal', session:Session, source:Message):
        self._clientInternal = clientInternal
        self._session = session
        self._source = source

        mr  = MqUtils.getOf(source)

        self._sender = mr.getSender(source)

        self._key = mr.getKey(source)
        self._tag = mr.getTag(source)
        self._fullTopic = mr.getTopic(source)
        self._topic = MqTopicHelper.getTopic(self._fullTopic)
        self._consumerGroup = mr.getConsumerGroup(source)

        self._qos = mr.getQos(source)
        self._times = mr.getTimes(source)
        self._sequence = mr.isSequence(source)
        self._transaction = mr.isTransaction(source)

        self._expirationL = mr.getExpiration(source)
        self._expiration = self._expirationL

    def getSource(self) -> Message:
        return self._source

    def getSender(self) -> str | None:
        return self._sender

    def getKey(self) -> str:
        return self._key

    def getTag(self) -> str:
        return self._tag

    def getTopic(self) -> str:
        return self._topic

    def getFullTopic(self)->str:
        return self._fullTopic

    def getConsumerGroup(self) -> str:
        return self._consumerGroup

    def getBody(self) -> bytes:
        return self._source.data_as_bytes()
    def getBodyAsString(self) -> str:
        return self._source.data_as_string()

    def getQos(self) -> int:
        return self._qos

    def getAttr(self, name: str) -> str | None:
        return self._source.meta(MqConstants.MQ_ATTR_PREFIX + name)

    def getExpiration(self) -> datetime | None:
        return self._expiration

    def isTransaction(self) -> bool:
        return self._transaction

    def isSequence(self) -> bool:
        return self._sequence

    def getTimes(self) -> int:
        return self._times

    def acknowledge(self, isOk: bool):
        self._clientInternal.reply(self._session, self._source, self, isOk, None)

    def response(self, entity:Entity):
        self._clientInternal.reply(self._session, self._source, self, True, entity)

    def __str__(self) ->str:
        return "MqMessageReceived{" + \
            "key='" + self._key + '\'' + \
            ", tag='" + self._tag + '\'' + \
            ", topic='" + self._topic + '\'' + \
            ", body='" + self.getBodyAsString() + '\'' + \
            '}'


