from datetime import datetime

from socketd.transport.utils.StrUtils import StrUtils

from folkmq.client.MqTransaction import MqTransaction


class MqMessageBase:
    #发送人
    @staticmethod
    def getSender(self) -> str | None: ...

    #主建
    @staticmethod
    def getKey(self) -> str: ...

    #标签
    @staticmethod
    def getTag(self) -> str: ...

    #内容
    @staticmethod
    def getBody(self) -> bytes: ...

    #过期时间
    @staticmethod
    def getExpiration(self) -> datetime | None: ...

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
    def __init__(self, body: str|bytes, key:str|None) :
        if key:
            self.__key = key
        else:
            self.__key = StrUtils.guid()

        if isinstance(str, body):
            self.__body = body.encode("utf-8")
        else:
            self.__body = body

        self.__sender = None;
        self.__tag = None;
        self.__scheduled:datetime = None
        self.__expiration:datetime = None;
        self.__sequence:bool = False
        self.__sequenceSharding: str | None = None
        self.__qos:int = 1

        self.__attrMap: dict[str, str] = {}
        self.__transaction:MqTransaction = None

    def getSender(self) -> str | None:
        return self.__sender

    def getKey(self) -> str:
        return self.__key

    def getTag(self) -> str | None:
        return self.__tag

    def getBody(self) -> bytes:
        return self.__body

    def getScheduled(self) -> datetime:
        return self.__scheduled

    def getExpiration(self) -> datetime:
        return self.__expiration

    def isTransaction(self) -> bool:
        return self.__transaction is not None

    def isSequence(self) -> bool:
        return self.__sequence

    def getSequenceSharding(self)->str:
        return self.__sequenceSharding

    def getQos(self) -> int:
        return self.__qos

    def tag(self, tag: str)->'MqMessage':
        self.__tag = tag
        return self

    def asJson(self)-> 'MqMessage':
        self.attr("Content-Type", "application/json")
        return self;



    def scheduled(self, scheduled: datetime)-> 'MqMessage':
        self._scheduled = scheduled
        return self


    def expiration(self,expiration: datetime)-> 'MqMessage':
        self._expiration = expiration
        return self

    def sequence(self, sequence: bool, sharding: str|None)-> 'MqMessage':
        self._sequence = sequence
        if sequence:
            if StrUtils.is_not_empty(sharding):
                self._sequenceSharding = sharding
        else:
            self._sequenceSharding = None
        return self


    def transaction(self, transaction: MqTransaction|None)-> 'MqMessage':
        if transaction is not None:
            self._transaction = transaction
            transaction.binding(self)

        return self

    def getTmid(self)-> str | None:
        if self.__transaction is None:
            return None
        else:
            return self.__transaction.tmid()

    def internalSender(self, sender: str)-> 'MqMessage':
        self._sender = sender
        return self

    def qos(self, qos: int)-> 'MqMessage':
        self._qos = qos
        return self

    def getAttr(self, name: str)-> str | None:
        tmp = self._attrMap.get(name);
        return tmp;


    def getAttrMap(self)-> dict[str,str]:
        return self._attrMap


    def attr(self, name: str, value: str)-> 'MqMessage':
        self._attrMap.set(name, value)
        return self



