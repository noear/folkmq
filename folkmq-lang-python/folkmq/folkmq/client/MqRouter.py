from typing import Union, Callable, Dict

from folkmq.client.MqMessageReceived import MqMessageReceived


# 消息路由器
class MqRouter:
    def __init__(self, mappingHandler: Callable[[MqMessageReceived], str]):
        self._mappingHandler:Callable[[MqMessageReceived], str] = mappingHandler
        self._mappingMap:Dict[str,str] = {}
        self._consumeHandler:Union[Callable[[MqMessageReceived], None], None] = None

    # 添加映射处理
    def do_on(self, mapping:str, consumeHandler:Callable[[MqMessageReceived], None]) -> 'MqRouter':
        self._mappingMap[mapping] = consumeHandler
        return self

    # 添加消费处理
    def do_on_consume(self, consumeHandler:Callable[[MqMessageReceived], None]) -> 'MqRouter':
        self._consumeHandler = consumeHandler
        return self

    # 消费
    def consume(self, message:MqMessageReceived):
        if self._consumeHandler:
            self._consumeHandler(message)

        mapping = self._mappingHandler(message)
        handler = self._mappingMap.get(mapping)
        if handler:
            handler(message)