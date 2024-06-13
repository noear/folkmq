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
        self.__clientInternal = clientInternal
        self.__session = session
        self.__source = source

        mr  = MqUtils.get_of(source)

        self.__sender = mr.get_sender(source)

        self.__key = mr.get_key(source)
        self.__tag = mr.get_tag(source)
        self.__fullTopic = mr.get_topic(source)
        self.__topic = MqTopicHelper.get_topic(self.__fullTopic)
        self.__consumerGroup = mr.get_consumer_group(source)

        self.__qos = mr.get_qos(source)
        self.__times = mr.get_times(source)
        self.__sequence = mr.is_sequence(source)
        self.__broadcast = mr.is_broadcast(source)
        self.__transaction = mr.is_transaction(source)

        self.__expirationL = mr.get_expiration(source)
        self.__expiration = self.__expirationL

        self.__replied = False

    def is_replied(self) -> bool:
        return self.__replied

    def set_replied(self, replied: bool):
        self.__replied = replied

    def get_source(self) -> Message:
        return self.__source

    def get_sender(self) -> str | None:
        return self.__sender

    def get_key(self) -> str:
        return self.__key

    def get_tag(self) -> str:
        return self.__tag

    def get_topic(self) -> str:
        return self.__topic

    def get_full_topic(self)->str:
        return self.__fullTopic

    def get_consumer_group(self) -> str:
        return self.__consumerGroup

    def get_body(self) -> bytes:
        return self.__source.data_as_bytes()

    def get_body_as_string(self) -> str:
        return self.__source.data_as_string()

    def get_qos(self) -> int:
        return self.__qos

    def get_attr(self, name: str) -> str | None:
        return self.__source.meta(MqConstants.MQ_ATTR_PREFIX + name)

    def get_expiration(self) -> datetime | None:
        return self.__expiration

    def is_transaction(self) -> bool:
        return self.__transaction

    def is_sequence(self) -> bool:
        return self.__sequence

    def is_broadcast(self) -> bool:
        return self.__broadcast

    def get_times(self) -> int:
        return self.__times

    def acknowledge(self, isOk: bool):
        self.__clientInternal.reply(self.__session, self, isOk, None)

    def response(self, entity:Entity):
        self.__clientInternal.reply(self.__session, self, True, entity)

    def __str__(self) ->str:
        return "MqMessageReceived{" + \
            "key='" + self.__key + '\'' + \
            ", tag='" + self.__tag + '\'' + \
            ", topic='" + self.__topic + '\'' + \
            ", body='" + self.get_body_as_string() + '\'' + \
            '}'


