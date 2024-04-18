from asyncio import Future
from typing import Callable

from socketd.transport.client.ClientConfig import ClientConfig
from socketd.transport.core import Entity
from socketd.transport.core.Message import Message
from socketd.transport.core.Session import Session
from socketd.transport.stream.RequestStream import RequestStream

from folkmq.client.MqClient import MqClientInternal
from folkmq.client.MqMessage import MqMessage
from folkmq.client.MqMessageReceived import MqMessageReceived, MqMessageReceivedImpl
from folkmq.client.MqTransaction import MqTransaction

# 消息客户端默认实现
class MqClientDefault(MqClientInternal):
    def __init__(self, *urls):
        ...

    def name(self) -> str:
        pass

    def nameAs(self, name: str) -> 'MqClient':
        pass

    def namespace(self) -> str:
        pass

    def namespaceAs(self, namespace: str) -> 'MqClient':
        pass

    def connect(self) -> 'MqClient' | Future:
        pass

    def disconnect(self):
        pass

    def config(self, configHandler: Callable[[ClientConfig], None]) -> 'MqClient':
        pass

    def autoAcknowledge(self, auto: bool) -> 'MqClient':
        pass

    def subscribe(self, topic: str, consumerGroup: str | None, autoAck: bool | None,
                  consumerHandler: Callable[[MqMessageReceived], None]):
        pass

    def unsubscribe(self, topic: str, consumerGroup: str | None):
        pass

    def publish(self, topic: str, message: MqMessage):
        pass

    def unpublish(self, topic: str, key: str):
        pass

    def listen(self, listenHandler: Callable[[MqMessageReceived], None]):
        pass

    def send(self, message: MqMessage, toName: str, timeout: int | None) -> RequestStream | None:
        pass

    def transactionCheckback(self, transactionCheckback: Callable[[MqMessageReceived], None]):
        pass

    def newTransaction(self) -> MqTransaction:
        pass

    def publish2(self, tmid: str, *keyAry: str, isRollback: bool):
        pass

    def reply(self, session: Session, f: Message, message: MqMessageReceivedImpl, isOk: bool, entity: Entity | None):
        pass