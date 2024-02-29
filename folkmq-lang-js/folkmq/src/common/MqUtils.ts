import {Message, MessageDefault} from "@noear/socket.d/transport/core/Message";
import {MqConstants} from "./MqConstants";
import {MqMetasResolverV2} from "./MqMetasResolverV2";
import {MqMetasResolverV1} from "./MqMetasResolverV1";
import {MqMetasResolver} from "./MqMetasResolver";
import {Session} from "@noear/socket.d/transport/core/Session";
import {MqMetasV2} from "./MqMetasV2";

/**
 * 消息工具类
 *
 * @author noear
 * @since 1.0
 * @since 1.2
 */
export class MqUtils {
    private static v1 = new MqMetasResolverV1();
    private static v2 = new MqMetasResolverV2();

    static getV2(): MqMetasResolver {
        return MqUtils.v2;
    }

    static getOf(t: Session | Message | null): MqMetasResolver {
        if (t == null) {
            return MqUtils.v2;
        } else {
            if (t instanceof MessageDefault) {
                let m = t as Message;
                let ver = m.metaOrDefault(MqMetasV2.MQ_META_VID, "1");
                if ("1" == ver) {
                    return MqUtils.v1;
                } else {
                    return MqUtils.v2;
                }
            } else {
                let s = t as Session;
                let ver = s.handshake().paramOrDefault(MqConstants.FOLKMQ_VERSION, "1");
                if ("1" == ver) {
                    return MqUtils.v1;
                } else {
                    return MqUtils.v2;
                }
            }
        }
    }
}