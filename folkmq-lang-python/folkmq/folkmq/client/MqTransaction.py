from abc import abstractmethod, ABC

from socketd.utils.StrUtils import StrUtils


class MqTransaction(ABC):
    @abstractmethod
    def tmid(self) -> str:
        """事务管理id"""
        ...

    @abstractmethod
    def binding(self, message: 'MqMessage'):
        """事务绑定"""
        ...

    @abstractmethod
    async def commit(self):
        """事务提交"""
        ...

    @abstractmethod
    async def rollback(self):
        """事务回滚"""
        ...


class MqTransactionImpl(MqTransaction):
    def __init__(self, client: 'MqClientInternal'):
        self._client = client
        self._keyAry:[str] = []
        self._tmid:str = StrUtils.guid()

    def tmid(self) -> str:
        return self._tmid

    def binding(self, message: 'MqMessage'):
        self._keyAry.append(message.get_key())
        message.internal_sender(self._client.name())

    async def commit(self):
        await self._client.publish2(self._tmid, self._keyAry, False)

    async def rollback(self):
        await self._client.publish2(self._tmid, self._keyAry, True)