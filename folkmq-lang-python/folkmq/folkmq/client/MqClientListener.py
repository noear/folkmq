from socketd.transport.core.listener.EventListener import EventListener

from folkmq.client.MqClient import MqClient
from folkmq.client.MqClientDefault import MqClientDefault


class MqClientListener(EventListener):
    def __init__(self, client:MqClientDefault):
        _client = client