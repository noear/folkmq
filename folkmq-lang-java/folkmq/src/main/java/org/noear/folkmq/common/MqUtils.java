package org.noear.folkmq.common;

import org.noear.socketd.transport.core.Entity;
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
    private static MqMetasResolver v3 = new MqMetasResolverV3();

    public static MqMetasResolver getLast() {
        return v2;
    }

    public static MqMetasResolver getOf(Session s) {
        //def=1
        String ver = s.handshake().paramOrDefault(MqConstants.FOLKMQ_VERSION, "1");
        return resolve(ver);
    }

    public static MqMetasResolver getOf(Entity m) {
        if (m == null) {
            return getLast();
        }

        //def=1
        String ver = m.metaOrDefault(MqMetasV2.MQ_META_VID, "1");
        return resolve(ver);
    }

    private static MqMetasResolver resolve(String ver){
        if ("1".equals(ver)) {
            return v1;
        } else if ("2".equals(ver)) {
            return v2;
        } else {
            return v3;
        }
    }
}