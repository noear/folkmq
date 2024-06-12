from abc import abstractmethod, ABC
from asyncio import Future
from typing import Callable

from socketd.transport.client.ClientConfig import ClientConfig
from socketd.transport.core import Entity
from socketd.transport.core.Message import Message
from socketd.transport.core.Session import Session
from socketd.transport.stream.RequestStream import RequestStream
from socketd.utils.CompletableFuture import CompletableFuture

from folkmq.client.MqMessage import MqMessage
from folkmq.client.MqMessageReceived import MqMessageReceived, MqMessageReceivedImpl
from folkmq.client.MqTransaction import MqTransaction

# 消息客户端
class MqClient(ABC):
    @abstractmethod
    def name(self) -> str:
        """名字（即，默认消费者组）"""
        ...

    @abstractmethod
    def name_as(self, name: str) -> 'MqClient':
        """名字取为（即，默认消费者组）"""
        ...

    @abstractmethod
    def namespace(self) -> str:
        """命名空间"""
        ...

    @abstractmethod
    def namespace_as(self, namespace: str) -> 'MqClient':
        """命名空间"""
        ...

    @abstractmethod
    async def connect(self) -> 'MqClient':
        """连接"""
        ...

    @abstractmethod
    async def disconnect(self):
        """断开连接"""
        ...

    @abstractmethod
    def config(self, configHandler: Callable[[ClientConfig], None]) -> 'MqClient':
        """客户端配置"""
        ...

    @abstractmethod
    def auto_acknowledge(self, auto: bool) -> 'MqClient':
        """
        自动回执
        :param auto: 自动（默认为 true）
        """
        ...

    @abstractmethod
    async def subscribe(self, topic: str, consumerGroup: str | None, autoAck: bool | None,
                  consumerHandler: Callable[[MqMessageReceived], None]):
        """
        订阅主题
        :param topic:           主题
        :param consumerGroup:   消费者组
        :param consumerHandler: 消费处理
        """
        ...

    @abstractmethod
    async def unsubscribe(self, topic: str, consumerGroup: str | None):
        """
        取消订阅主题
        param topic:         主题
        param consumerGroup: 消费者组
        """
        ...

    @abstractmethod
    async def publish(self, topic: str, message: MqMessage):
        """
        同步发布消息
        :param topic:   主题
        :param message: 消息
        """
        ...

    def publish_async(self, topic: str, message: MqMessage) -> CompletableFuture:
        """
        异步发布消息
        :param topic:   主题
        :param message: 消息
        """
        ...

    @abstractmethod
    async def unpublish(self, topic: str, key: str):
        """
        取消发布
        :param topic: 主题
        :param key:   主建
        """
        ...


    @abstractmethod
    async def listen(self, listenHandler: Callable[[MqMessageReceived], None]):
        """
        监听
        :param listenHandler: 监听处理
        :return:
        """
        ...


    @abstractmethod
    def send(self, message: MqMessage, toName: str, timeout: int | None) -> RequestStream | None:
        """发送
        :param message:消息
        :param toName:发送目标名字
        :param timeout:超时（单位毫秒）
        """
        ...

    @abstractmethod
    def transaction_checkback(self, transactionCheckback: Callable[[MqMessageReceived], None]) -> 'MqClient':
        """事务回查
        :param transactionCheckback: 事务回查处理
        """
        ...


    @abstractmethod
    def new_transaction(self) -> MqTransaction:
        """新建事务"""
        ...


class MqClientInternal(MqClient):
    @abstractmethod
    async def publish2(self, tmid: str, keyAry: [str], isRollback: bool):
        """发布二次提交"""
        ...

    @abstractmethod
    def reply(self, session: Session, message: MqMessageReceivedImpl, isOk: bool, entity: Entity):
        """消费答复"""
        ...