import {MqMessage} from "./MqMessage";
import {MqClientInternal} from "./MqClient";
import {StrUtils} from "@noear/socket.d/utils/StrUtils";

/**
 * 事务
 *
 * @author noear
 * @since 1.2
 */
export interface MqTransaction {
    /**
     * 事务管理id
     */
    tmid(): string;

    /**
     * 事务绑定
     * */
    binding(message: MqMessage);

    /**
     * 事务提交
     */
    commit();

    /**
     * 事务回滚
     */
    rollback();
}

/**
 * 事务实现
 *
 * @author noear
 * @since 1.2
 */
export class MqTransactionImpl implements MqTransaction {
    private readonly _client: MqClientInternal;
    private readonly _tidAry;
    private readonly _tmid: string;

    constructor(client: MqClientInternal) {
        this._client = client;
        this._tidAry = new Array<string>();
        this._tmid = StrUtils.guid();
    }

    tmid(): string {
        return this._tmid;
    }

    binding(message: MqMessage) {
        this._tidAry.push(message.getTid());
        message.internalSender(this._client.name());
    }

    commit() {
        this._client.publish2(this._tmid, this._tidAry, false);
    }

    rollback() {
        this._client.publish2(this._tmid, this._tidAry, true);
    }
}