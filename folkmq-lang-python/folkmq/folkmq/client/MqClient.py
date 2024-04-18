from abc import abstractmethod
from asyncio import Future
from typing import Callable

from socketd.transport.client.ClientConfig import ClientConfig
from socketd.transport.core import Entity
from socketd.transport.core.Message import Message
from socketd.transport.core.Session import Session
from socketd.transport.stream.RequestStream import RequestStream

from folkmq.client.MqMessage import MqMessage
from folkmq.client.MqMessageReceived import MqMessageReceived, MqMessageReceivedImpl
from folkmq.client.MqTransaction import MqTransaction

# 消息客户端
class MqClient:
    #
    # 名字（即，默认消费者组）
    #
    @abstractmethod
    def name(self) -> str: ...


    #
    # 名字取为（即，默认消费者组）
    #
    @abstractmethod
    def nameAs(self, name: str) -> 'MqClient': ...


    #
    # 命名空间
    #
    @abstractmethod
    def namespace(self) -> str: ...


    #
    # 命名空间
    # @since 1.4
    #
    @abstractmethod
    def namespaceAs(self, namespace: str) -> 'MqClient': ...


    #
    # 连接
    #
    @abstractmethod
    def connect(self) -> 'MqClient' | Future: ...


    #
    # 断开连接
    #
    @abstractmethod
    def disconnect(self): ...


    #
    # 客户端配置
    #
    @abstractmethod
    def config(self, configHandler: Callable[[ClientConfig], None]) -> 'MqClient': ...


    #
    # 自动回执
    #
    # @param auto 自动（默认为 true）
    #
    @abstractmethod
    def autoAcknowledge(self, auto: bool) -> 'MqClient': ...


    #
    # 订阅主题
    #
    # @param topic           主题
    # @param consumerGroup   消费者组
    # @param consumerHandler 消费处理
    #
    @abstractmethod
    def subscribe(self, topic: str, consumerGroup: str | None, autoAck: bool | None,
                  consumerHandler: Callable[[MqMessageReceived], None]): ...


    #
    # 取消订阅主题
    #
    # @param topic         主题
    # @param consumerGroup 消费者组
    #
    @abstractmethod
    def unsubscribe(self, topic: str, consumerGroup: str | None): ...


    #
    # 同步发布消息
    #
    # @param topic   主题
    # @param message 消息
    #
    @abstractmethod
    def publish(self, topic: str, message: MqMessage): ...


    #
    # 取消发布
    #
    # @param topic 主题
    # @param key   主建
    #
    @abstractmethod
    def unpublish(self, topic: str, key: str): ...


    #
    # 监听
    #
    # @param listenHandler 监听处理
    #
    @abstractmethod
    def listen(self, listenHandler: Callable[[MqMessageReceived], None]): ...


    #
    # 发送
    #
    # @param message 消息
    # @param toName  发送目标名字
    # @param timeout 超时（单位毫秒）
    #
    @abstractmethod
    def send(self, message: MqMessage, toName: str, timeout: int | None) -> RequestStream | None: ...


    #
    # 事务回查
    #
    # @param transactionCheckback 事务回查处理
    #
    @abstractmethod
    def transactionCheckback(self, transactionCheckback: Callable[[MqMessageReceived], None]): ...


    #
    # 新建事务
    #
    @abstractmethod
    def newTransaction(self) -> MqTransaction: ...


class MqClientInternal(MqClient):
    #发布二次提交
    @abstractmethod
    def publish2(self, tmid: str, *keyAry: str, isRollback: bool):...

    #消费答复
    @abstractmethod
    def reply(self, session: Session, f: Message, message: MqMessageReceivedImpl, isOk: bool, entity: Entity | None):...