class MqAssert:
    @staticmethod
    def requireNonNull(obj, message:str):
        if obj is None:
            raise Exception(message)
        else:
            return obj
    @staticmethod
    def assertMeta(str:str, paramName:str):
        MqAssert.assertMetaSymbols(str, paramName, '?', "?")
        MqAssert.assertMetaSymbols(str, paramName, '&', "&")
        MqAssert.assertMetaSymbols(str, paramName, '=', "=")
        MqAssert.assertMetaSymbols(str, paramName, '!', "!")

        MqAssert.assertMetaSymbols(str, paramName, '@', "@")
        MqAssert.assertMetaSymbols(str, paramName, '#', "#")
        MqAssert.assertMetaSymbols(str, paramName, '$', "$")
        MqAssert.assertMetaSymbols(str, paramName, '%', "%")
        MqAssert.assertMetaSymbols(str, paramName, '^', "^")
        MqAssert.assertMetaSymbols(str, paramName, '*', "*")

    @staticmethod
    def assertMetaSymbols(str: str, paramName: str, c:str, cS:str):
        if str.find(c) >= 0:
            raise Exception("Param '" + paramName + "' can't have symbols: '" + cS + "'");
