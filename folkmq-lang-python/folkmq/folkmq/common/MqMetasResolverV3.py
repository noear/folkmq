from folkmq.common.MqMetasResolverV2 import MqMetasResolverV2


class MqMetasResolverV3(MqMetasResolverV2):
    def version(self) -> int:
        return 3
