from datetime import datetime
from io import BytesIO


class MqMessageBase:
    #发送人
    @staticmethod
    def getSender(self) -> str: ...

    #主建
    @staticmethod
    def getKey(self) -> str: ...

    #标签
    @staticmethod
    def getTag(self) -> str: ...

    #内容
    @staticmethod
    def getBody(self) -> BytesIO: ...

    #过期时间
    @staticmethod
    def getExpiration(self) -> datetime: ...

    #是否事务
    @staticmethod
    def isTransaction(self) -> bool: ...

    #是否有序
    @staticmethod
    def isSequence(self) -> bool: ...

    #质量等级
    @staticmethod
    def getQos(self) -> int: ...

    #获取属性
    @staticmethod
    def getAttr(self, name: str) -> str | None: ...


class MqMessage(MqMessageBase):
    def __init__(self, body: str|BytesIO, key:str|None) :
        self.__key = key
        self.__body = body

        self.__sender = None;
        self.__tag = None;
        self.__scheduled:datetime = None
        self.__expiration:datetime = None;
        self.__isTransaction = False
        self.__isSequence = False

    def getSender(self) -> str:
        return self.__sender;

