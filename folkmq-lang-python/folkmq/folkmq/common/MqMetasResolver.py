import abc

from socketd.transport.core import Entity
from socketd.transport.core.Message import Message
from socketd.transport.core.entity.EntityDefault import EntityDefault

from folkmq.client.MqMessage import MqMessage


class MqMetasResolver:
    #
    # 版本号
    #
    @abc.abstractmethod
    def version(self)-> int:...

    #
    # 获取发送人
    #
    @abc.abstractmethod
    def get_sender(self, m: Entity)-> str:...

    #
    # 获取主建
    #
    @abc.abstractmethod
    def get_key(self, m: Entity)-> str:...

    #
    # 获取标签
    #
    @abc.abstractmethod
    def get_tag(self, m: Entity)-> str:...

    #
    # 获取主题
    #
    @abc.abstractmethod
    def get_topic(self, m: Entity)-> str:...

    #
    # 获取消费者组
    #
    @abc.abstractmethod
    def get_consumer_group(self, m: Entity)-> str:...

    #
    # 设置消费者组
    #
    @abc.abstractmethod
    def set_consumer_group(self, m: Entity, consumerGroup: str):...

    #
    # 获取质量等级（0或1）
    #
    @abc.abstractmethod
    def get_qos(self, m: Entity)-> int:...

    #
    # 获取派发次数
    #
    @abc.abstractmethod
    def get_times(self, m: Entity)-> int:...

    #
    # 设置派发次数
    #
    @abc.abstractmethod
    def set_times(self, m: Entity, times: int):...

    #
    # 获取过期时间
    #
    @abc.abstractmethod
    def get_expiration(self, m: Entity)->float:...

    #
    # 设置过期时间
    #
    @abc.abstractmethod
    def set_expiration(self, m: Entity, expiration: int):...

    #
    # 获取定时时间
    #
    @abc.abstractmethod
    def get_scheduled(self, m: Entity)-> int:...

    #
    # 设置定时时间
    #
    @abc.abstractmethod
    def set_scheduled(self, m: Entity, scheduled: int):...

    #
    # 是否有序
    #
    @abc.abstractmethod
    def is_sequence(self, m: Entity)->bool:...

    #
    # 是否广播
    #
    @abc.abstractmethod
    def is_broadcast(self, m: Entity)->bool:...

    #
    # 是否事务
    #
    @abc.abstractmethod
    def is_transaction(self, m: Entity)->bool:...

    #
    # 设置事务
    #
    @abc.abstractmethod
    def set_transaction(self, m: Entity, isTransaction: bool):...

    #
    # 发布实体构建
    #
    # @param topic   主题
    # @param message 消息
    #
    @abc.abstractmethod
    def publish_entity_build(self, topic: str, message: MqMessage)-> EntityDefault:...

    #
    # 路由消息构建
    #
    # @param topic   主题
    # @param message 消息
    #
    @abc.abstractmethod
    def routing_message_build(self, topic: str, message: MqMessage)-> Message:...