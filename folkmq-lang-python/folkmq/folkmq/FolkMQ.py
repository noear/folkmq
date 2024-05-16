from folkmq.client.MqClient import MqClient
from folkmq.client.MqClientDefault import MqClientDefault
from folkmq.client.MqMessage import MqMessage


class FolkMQ:
    @staticmethod
    def version_code()->int:
        return 2

    @staticmethod
    def version_code_as_string()->str:
        return f'{FolkMQ.version_code()}'

    @staticmethod
    def version_name()->str:
        return "1.4.5"

    @staticmethod
    def create_client(*serverUrls) -> MqClient:
        return MqClientDefault(serverUrls)


