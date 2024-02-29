/**
 * 断言
 *
 * @author noear
 * @since 1.2
 */
export class MqAssert {
    static requireNonNull(obj: any, message: string) : any{
        if (!obj) {
            throw new Error(message)
        }
        return obj;
    }

    /**
     * 断言元信息相关的名字与值
     */
    static assertMeta(str: string, paramName: string) {
        MqAssert.assertMetaSymbols(str, paramName, '?', "?");
        MqAssert.assertMetaSymbols(str, paramName, '&', "&");
        MqAssert.assertMetaSymbols(str, paramName, '=', "=");
        MqAssert.assertMetaSymbols(str, paramName, '!', "!");

        MqAssert.assertMetaSymbols(str, paramName, '@', "@");
        MqAssert.assertMetaSymbols(str, paramName, '#', "#");
        MqAssert.assertMetaSymbols(str, paramName, '$', "$");
        MqAssert.assertMetaSymbols(str, paramName, '%', "%");
        MqAssert.assertMetaSymbols(str, paramName, '^', "^");
        MqAssert.assertMetaSymbols(str, paramName, '*', "*");
    }

    static assertMetaSymbols(str: string, paramName: string, c: string, cS: string) {
        if (str.indexOf(c) >= 0) {
            throw new Error("Param '" + paramName + "' can't have symbols: '" + cS + "'");
        }
    }
}