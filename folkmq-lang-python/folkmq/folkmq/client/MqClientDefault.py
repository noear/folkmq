from asyncio import Future
from typing import Callable

from socketd import SocketD
from socketd.cluster.ClusterClientSession import ClusterClientSession
from socketd.transport.client.ClientConfig import ClientConfig
from socketd.transport.core import Entity
from socketd.transport.core.Message import Message
from socketd.transport.core.Session import Session
from socketd.transport.stream.RequestStream import RequestStream

from folkmq.FolkMQ import FolkMQ
from folkmq.client.MqClient import MqClientInternal
from folkmq.client.MqClientListener import MqClientListener
from folkmq.client.MqMessage import MqMessage
from folkmq.client.MqMessageReceived import MqMessageReceived, MqMessageReceivedImpl
from folkmq.client.MqTransaction import MqTransaction
from folkmq.common.MqConstants import MqConstants


# 消息客户端默认实现
class MqClientDefault(MqClientInternal):
    def __init__(self, *urls, clientListener:MqClientListener):
        self._urls = urls

        if clientListener:
            self._clientListener = clientListener
        else:
            self._clientListener =  MqClientListener()

        self._clientListener.init(self)

        self._name:str|None = None
        self._namespace:str|None = None
        self._clientSession:ClusterClientSession|None = None

    def name(self) -> str:
        return self._name

    def nameAs(self, name: str) -> 'MqClient':
        self._name = name
        return self

    def namespace(self) -> str:
        return self._namespace

    def namespaceAs(self, namespace: str) -> 'MqClient':
        self._namespace = namespace
        return self

    async def connect(self) -> 'MqClient' | Future:
        self._clientSession = await (SocketD.create_cluster_client(self._urls)
            .config(self.__config_handle)
            .listen(self._clientListener)
            .open())

        return self

    def __config_handle(self, c:ClientConfig):
        (c.meta_put(MqConstants.FOLKMQ_VERSION, FolkMQ.versionCodeAsString())
          .heartbeat_interval(6_000)
          .io_threads(1)
          .codec_threads(1)
          .exchange_threads(1))

        if self._namespace:
            c.meta_put(MqConstants.FOLKMQ_NAMESPACE, self._namespace)

        if self._clientConfigHandler:
            self._clientConfigHandler(c)

    def disconnect(self):
        self._clientSession.close()

    def config(self, configHandler: Callable[[ClientConfig], None]) -> 'MqClient':
        self._clientConfigHandler = configHandler
        return self

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