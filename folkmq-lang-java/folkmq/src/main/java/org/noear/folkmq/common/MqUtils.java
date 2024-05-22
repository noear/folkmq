package org.noear.folkmq.common;

import org.noear.socketd.transport.core.Message;
import org.noear.socketd.transport.core.Session;

/**
 * 消息工具类
 *
 * @author noear
 * @since 1.0
 * @since 1.2
 */
public class MqUtils {
    private static MqMetasResolver v1 = new MqMetasResolverV1();
    private static MqMetasResolver v2 = new MqMetasResolverV2();

    public static MqMetasResolver getV2() {
        return v2;
    }

    public static MqMetasResolver getOf(Session s) {
        String ver = s.handshake().paramOrDefault(MqConstants.FOLKMQ_VERSION, "1");
        if ("1".equals(ver)) {
            return v1;
        } else {
            return v2;
        }
    }

    public static MqMetasResolver getOf(Message m) {
        if (m == null) {
            return v2;
        }

        String ver = m.metaOrDefault(MqMetasV2.MQ_META_VID, "1");
        if ("1".equals(ver)) {
            return v1;
        } else {
            return v2;
        }
    }

    /**
     * 允许发送
     */
    public static boolean allowSend(Session s) {
        if (s == null) {
            return false;
        } else {
            return s.isValid() && s.isClosing() == false;
        }
    }
}