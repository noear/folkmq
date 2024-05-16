class MqAssert:
    @staticmethod
    def require_non_null(obj, message:str):
        if obj is None:
            raise Exception(message)
        else:
            return obj
    @staticmethod
    def assert_meta(str:str, paramName:str):
        MqAssert.assert_meta_symbols(str, paramName, '?', "?")
        MqAssert.assert_meta_symbols(str, paramName, '&', "&")
        MqAssert.assert_meta_symbols(str, paramName, '=', "=")
        MqAssert.assert_meta_symbols(str, paramName, '!', "!")

        MqAssert.assert_meta_symbols(str, paramName, '@', "@")
        MqAssert.assert_meta_symbols(str, paramName, '#', "#")
        MqAssert.assert_meta_symbols(str, paramName, '$', "$")
        MqAssert.assert_meta_symbols(str, paramName, '%', "%")
        MqAssert.assert_meta_symbols(str, paramName, '^', "^")
        MqAssert.assert_meta_symbols(str, paramName, '*', "*")

    @staticmethod
    def assert_meta_symbols(str: str, paramName: str, c:str, cS:str):
        if str.find(c) >= 0:
            raise Exception("Param '" + paramName + "' can't have symbols: '" + cS + "'");
