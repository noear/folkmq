from abc import abstractmethod
from datetime import datetime

from socketd.utils.StrUtils import StrUtils

from folkmq.client.MqTransaction import MqTransaction


class MqMessageBase:
    #发送人
    @abstractmethod
    def get_sender(self) -> str | None: ...

    #主建
    @abstractmethod
    def get_key(self) -> str: ...

    #标签
    @abstractmethod
    def get_tag(self) -> str: ...

    #内容
    @abstractmethod
    def get_body(self) -> bytes: ...

    #过期时间
    @abstractmethod
    def get_expiration(self) -> datetime | None: ...

    #是否事务
    @abstractmethod
    def is_transaction(self) -> bool: ...

    #是否有序
    @abstractmethod
    def is_sequence(self) -> bool: ...

    # 是否广播
    @abstractmethod
    def is_broadcast(self) -> bool: ...

    #质量等级
    @abstractmethod
    def get_qos(self) -> int: ...

    #获取属性
    @abstractmethod
    def get_attr(self, name: str) -> str | None: ...


class MqMessage(MqMessageBase):
    def __init__(self, body: str|bytes, key:str|None = None) :
        if key:
            self.__key = key
        else:
            self.__key = StrUtils.guid()

        if isinstance(body, str):
            self.__body = body.encode("utf-8")
        else:
            self.__body = body

        self.__sender = None;
        self.__tag = None;
        self.__scheduled:datetime = None
        self.__expiration:datetime = None;
        self.__sequence:bool = False
        self.__broadcast:bool = False
        self.__sequenceSharding: str | None = None
        self.__qos:int = 1

        self.__attrMap: dict[str, str] = {}
        self.__transaction:MqTransaction = None

    def get_sender(self) -> str | None:
        return self.__sender

    def get_key(self) -> str:
        return self.__key

    def get_tag(self) -> str | None:
        return self.__tag

    def get_body(self) -> bytes:
        return self.__body

    def getScheduled(self) -> datetime:
        return self.__scheduled

    def get_expiration(self) -> datetime:
        return self.__expiration

    def is_transaction(self) -> bool:
        return self.__transaction is not None

    def is_broadcast(self) -> bool:
        return self.__broadcast

    def is_sequence(self) -> bool:
        return self.__sequence

    def getSequenceSharding(self)->str:
        return self.__sequenceSharding

    def get_qos(self) -> int:
        return self.__qos

    def tag(self, tag: str)->'MqMessage':
        self.__tag = tag
        return self

    def as_json(self)-> 'MqMessage':
        self.attr("Content-Type", "application/json")
        return self;



    def scheduled(self, scheduled: datetime)-> 'MqMessage':
        self.__scheduled = scheduled
        return self


    def expiration(self,expiration: datetime)-> 'MqMessage':
        self.__expiration = expiration
        return self

    def sequence(self, sequence: bool, sharding: str|None)-> 'MqMessage':
        self.__sequence = sequence
        if sequence:
            if StrUtils.is_not_empty(sharding):
                self.__sequenceSharding = sharding
        else:
            self.__sequenceSharding = None
        return self

    def broadcast(self, broadcast: bool)-> 'MqMessage':
        self.__broadcast = broadcast
        return self


    def transaction(self, transaction: MqTransaction|None)-> 'MqMessage':
        if transaction is not None:
            self.__transaction = transaction
            transaction.binding(self)

        return self

    def get_tmid(self)-> str | None:
        if self.__transaction is None:
            return None
        else:
            return self.__transaction.tmid()

    def internal_sender(self, sender: str)-> 'MqMessage':
        self.__sender = sender
        return self

    def qos(self, qos: int)-> 'MqMessage':
        self.__qos = qos
        return self

    def get_attr(self, name: str)-> str | None:
        tmp = self.__attrMap.get(name);
        return tmp;


    def get_attr_map(self)-> dict[str,str]:
        return self.__attrMap


    def attr(self, name: str, value: str)-> 'MqMessage':
        self.__attrMap.set(name, value)
        return self



