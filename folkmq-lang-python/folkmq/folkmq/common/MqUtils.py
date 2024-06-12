from socketd.transport.core.Message import Message
from socketd.transport.core.Session import Session
from socketd.transport.core.entity.MessageDefault import MessageDefault

from folkmq.common.MqConstants import MqConstants
from folkmq.common.MqMetasResolver import MqMetasResolver
from folkmq.common.MqMetasResolverV1 import MqMetasResolverV1
from folkmq.common.MqMetasResolverV2 import MqMetasResolverV2
from folkmq.common.MqMetasResolverV3 import MqMetasResolverV3
from folkmq.common.MqMetasV2 import MqMetasV2

# 消息工具类
class MqUtils:
    __v1 = MqMetasResolverV1()
    __v2 = MqMetasResolverV2()
    __v3 = MqMetasResolverV3()

    @staticmethod
    def get_last() -> MqMetasResolver:
        return MqUtils.__v3

    @staticmethod
    def get_of(t: Session | Message | None) -> MqMetasResolver:
        if t is None:
            return MqUtils.__v2
        else:
            if isinstance(t, MessageDefault):
                ver = t.meta_or_default(MqMetasV2.MQ_META_VID, "1")
                return MqUtils.resolve(ver)
            else:
                ver = t.handshake().param_or_default(MqConstants.FOLKMQ_VERSION, "1")
                return MqUtils.resolve(ver)

    @staticmethod
    def resolve(ver:str) -> MqMetasResolver:
        if "1" == ver:
            return MqUtils.__v1
        elif "2" == ver:
            return MqUtils.__v2
        else:
            return MqUtils.__v3
