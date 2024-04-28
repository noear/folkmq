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
    def getSender(self, m: Entity)-> str:...

    #
    # 获取主建
    #
    @abc.abstractmethod
    def getKey(self, m: Entity)-> str:...

    #
    # 获取标签
    #
    @abc.abstractmethod
    def getTag(self, m: Entity)-> str:...

    #
    # 获取主题
    #
    @abc.abstractmethod
    def getTopic(self, m: Entity)-> str:...

    #
    # 获取消费者组
    #
    @abc.abstractmethod
    def getConsumerGroup(self, m: Entity)-> str:...

    #
    # 设置消费者组
    #
    @abc.abstractmethod
    def setConsumerGroup(self, m: Entity, consumerGroup: str):...

    #
    # 获取质量等级（0或1）
    #
    @abc.abstractmethod
    def getQos(self, m: Entity)-> int:...

    #
    # 获取派发次数
    #
    @abc.abstractmethod
    def getTimes(self, m: Entity)-> int:...

    #
    # 设置派发次数
    #
    @abc.abstractmethod
    def setTimes(self, m: Entity, times: int):...

    #
    # 获取过期时间
    #
    @abc.abstractmethod
    def getExpiration(self, m: Entity)->float:...

    #
    # 设置过期时间
    #
    @abc.abstractmethod
    def setExpiration(self, m: Entity, expiration: int):...

    #
    # 获取定时时间
    #
    @abc.abstractmethod
    def getScheduled(self, m: Entity)-> int:...

    #
    # 设置定时时间
    #
    @abc.abstractmethod
    def setScheduled(self, m: Entity, scheduled: int):...

    #
    # 是否有序
    #
    @abc.abstractmethod
    def isSequence(self, m: Entity)->bool:...

    #
    # 是否事务
    #
    @abc.abstractmethod
    def isTransaction(self, m: Entity)->bool:...

    #
    # 设置事务
    #
    @abc.abstractmethod
    def setTransaction(self, m: Entity, isTransaction: bool):...

    #
    # 发布实体构建
    #
    # @param topic   主题
    # @param message 消息
    #
    @abc.abstractmethod
    def publishEntityBuild(self, topic: str, message: MqMessage)-> EntityDefault:...

    #
    # 路由消息构建
    #
    # @param topic   主题
    # @param message 消息
    #
    @abc.abstractmethod
    def routingMessageBuild(self, topic: str, message: MqMessage)-> Message:...