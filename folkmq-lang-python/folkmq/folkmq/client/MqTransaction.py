from folkmq.client.MqMessage import MqMessage


class MqTransaction:
    def binding(self, message:MqMessage):
        ...
    def tmid(self)->str:
        ...