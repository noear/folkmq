import {MqMetasResolverV2} from "./MqMetasResolverV2";

export class MqMetasResolverV3 extends MqMetasResolverV2 {
    version(): number {
        return 3;
    }
}