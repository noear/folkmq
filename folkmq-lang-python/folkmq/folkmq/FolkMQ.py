from folkmq.client.MqClient import MqClient
from folkmq.client.MqClientDefault import MqClientDefault
from folkmq.client.MqMessage import MqMessage


class FolkMQ:
    @staticmethod
    def version_code()->int:
        return 3

    @staticmethod
    def version_code_as_string()->str:
        return f'{FolkMQ.version_code()}'

    @staticmethod
    def version_name()->str:
        return "1.7.0"

    @staticmethod
    def create_client(*serverUrls) -> MqClient:
        return MqClientDefault(serverUrls)

    @staticmethod
    def new_message(body: str|bytes, key:str|None = None):
        return MqMessage(body,key)


