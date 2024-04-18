from socketd.transport.core.entity.StringEntity import StringEntity


class MqAlarm(StringEntity):
    def __init__(self, data:str):
        super().__init__(data)