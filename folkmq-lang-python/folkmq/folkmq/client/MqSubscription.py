from typing import Callable

from folkmq.client.MqMessageReceived import MqMessageReceived
from folkmq.common.MqConstants import MqConstants


class MqSubscription:
    def __init__(self, topic: str, consumerGroup: str, autoAck: bool, consumeHandler:Callable[[MqMessageReceived], None]):
        self._topic = topic
        self._consumerGroup = consumerGroup
        self._autoAck = autoAck
        self._queueName = topic + MqConstants.SEPARATOR_TOPIC_CONSUMER_GROUP + consumerGroup
        self._consumeHandler = consumeHandler

    def getTopic(self) -> str:
        return self._topic

    def getConsumerGroup(self) -> str:
        return self._consumerGroup

    def isAutoAck(self) -> bool:
        return self._autoAck

    def getQueueName(self) -> str:
        return self._queueName

    def consume(self, message:MqMessageReceived):
        self._consumeHandler(message)