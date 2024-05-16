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
    def get_topic(self) -> str:
        ...

    # 内容
    @abstractmethod
    def get_body_as_string(self) -> str:
        ...

    # 消费者组
    @abstractmethod
    def get_consumer_group(self) -> str:
        ...

    # 已派发次数
    @abstractmethod
    def get_times(self) -> int:
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

        mr  = MqUtils.get_of(source)

        self._sender = mr.get_sender(source)

        self._key = mr.get_key(source)
        self._tag = mr.get_tag(source)
        self._fullTopic = mr.get_topic(source)
        self._topic = MqTopicHelper.get_topic(self._fullTopic)
        self._consumerGroup = mr.get_consumer_group(source)

        self._qos = mr.get_qos(source)
        self._times = mr.get_times(source)
        self._sequence = mr.is_sequence(source)
        self._transaction = mr.is_transaction(source)

        self._expirationL = mr.get_expiration(source)
        self._expiration = self._expirationL

    def getSource(self) -> Message:
        return self._source

    def get_sender(self) -> str | None:
        return self._sender

    def get_key(self) -> str:
        return self._key

    def get_tag(self) -> str:
        return self._tag

    def get_topic(self) -> str:
        return self._topic

    def getFullTopic(self)->str:
        return self._fullTopic

    def get_consumer_group(self) -> str:
        return self._consumerGroup

    def get_body(self) -> bytes:
        return self._source.data_as_bytes()
    def get_body_as_string(self) -> str:
        return self._source.data_as_string()

    def get_qos(self) -> int:
        return self._qos

    def get_attr(self, name: str) -> str | None:
        return self._source.meta(MqConstants.MQ_ATTR_PREFIX + name)

    def get_expiration(self) -> datetime | None:
        return self._expiration

    def is_transaction(self) -> bool:
        return self._transaction

    def is_sequence(self) -> bool:
        return self._sequence

    def get_times(self) -> int:
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
            ", body='" + self.get_body_as_string() + '\'' + \
            '}'


