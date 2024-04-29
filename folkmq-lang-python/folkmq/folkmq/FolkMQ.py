from folkmq.client.MqClient import MqClient
from folkmq.client.MqClientDefault import MqClientDefault
from folkmq.client.MqMessage import MqMessage


class FolkMQ:
    @staticmethod
    def versionCode()->int:
        return 2

    @staticmethod
    def versionCodeAsString()->str:
        return f'{FolkMQ.versionCode()}'

    @staticmethod
    def versionName()->str:
        return "1.4.3"

    @staticmethod
    def createClient(*serverUrls) -> MqClient:
        return MqClientDefault(serverUrls)


