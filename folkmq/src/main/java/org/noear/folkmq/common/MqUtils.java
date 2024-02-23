package org.noear.folkmq.common;

import org.noear.socketd.transport.core.Message;
import org.noear.socketd.transport.core.Session;

/**
 * 消息工具类
 *
 * @author noear
 * @see 1.0
 */
public class MqUtils {
    private static MqResolver v1 = new MqResolverV1();
    private static MqResolver v2 = new MqResolverV2();

    public static MqResolver getV2() {
        return v2;
    }

    public static MqResolver getOf(Session s) {
        String ver = s.handshake().paramOrDefault(MqConstants.FOLKMQ_VERSION, "1");
        if ("1".equals(ver)) {
            return v1;
        } else {
            return v2;
        }
    }

    public static MqResolver getOf(Message m) {
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
}