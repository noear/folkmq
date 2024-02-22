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
     * */
    public static void assertMeta(String str, String paramName) {
        if (str.indexOf('?') >= 0) {
            throw new IllegalArgumentException("Param '" + paramName + "' can't have symbols: '?'");
        }

        if (str.indexOf('&') >= 0) {
            throw new IllegalArgumentException("Param '" + paramName + "' can't have symbols: '&'");
        }

        if (str.indexOf('=') >= 0) {
            throw new IllegalArgumentException("Param '" + paramName + "' can't have symbols: '='");
        }

        if (str.indexOf('!') >= 0) {
            throw new IllegalArgumentException("Param '" + paramName + "' can't have symbols: '!'");
        }
    }
}
