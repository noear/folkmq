package org.noear.folkmq.common;

/**
 * 断言
 *
 * @author noear
 * @since 1.2
 */
public class MqAssert {
    /**
     * 断言元信息相关的名字与值
     */
    public static void assertMeta(String str, String paramName) {
        assertMetaSymbols(str, paramName, '?', "?");
        assertMetaSymbols(str, paramName, '&', "&");
        assertMetaSymbols(str, paramName, '=', "=");
        assertMetaSymbols(str, paramName, '!', "!");

        assertMetaSymbols(str, paramName, '@', "@");
        assertMetaSymbols(str, paramName, '#', "#");
        assertMetaSymbols(str, paramName, '$', "$");
        assertMetaSymbols(str, paramName, '%', "%");
        assertMetaSymbols(str, paramName, '^', "^");
        assertMetaSymbols(str, paramName, '*', "*");
    }

    public static void assertMetaSymbols(String str, String paramName, char c, String cS) {
        if (str.indexOf(c) >= 0) {
            throw new IllegalArgumentException("Param '" + paramName + "' can't have symbols: '" + cS + "'");
        }
    }
}
