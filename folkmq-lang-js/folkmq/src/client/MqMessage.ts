import {StrUtils} from "@noear/socket.d/utils/StrUtils";
import {MqTransaction} from "./MqTransaction";
import {Buffer, ByteBuffer} from "@noear/socket.d/transport/core/Buffer";

export interface MqMessageBase {
    /**
     * 发送人
     * */
    getSender(): string | null;

    /**
     * 跟踪ID
     */
    getTid(): string;

    /**
     * 标签
     * */
    getTag(): string | null;

    /**
     * 内容
     * */
    getBody(): Buffer;

    /**
     * 过期时间 //::long
     */
    getExpiration(): Date | null;

    /**
     * 是否事务
     */
    isTransaction(): boolean;

    /**
     * 是否有序
     */
    isSequence(): boolean;

    /**
     * 质量等级（0 或 1） //:int
     */
    getQos(): number;

    /**
     * 获取属性
     */
    getAttr(name: string): string | null;
}

export class MqMessage implements MqMessageBase {
    private readonly _tid: string;
    private readonly _body: ByteBuffer;

    private _sender: string | null = null;
    private _tag: string | null = null;
    private _scheduled: Date | null = null;
    private _expiration: Date | null = null;
    private _sequence: boolean = false;
    private _sequenceSharding: string | null = null;
    private _qos: number = 1;

    protected _attrMap = new Map<string, string>;
    protected _transaction: MqTransaction;

    constructor(body: string | ArrayBuffer) {
        this._tid = StrUtils.guid();
        if (body instanceof ArrayBuffer) {
            this._body = new ByteBuffer(body);
        } else {
            this._body = new ByteBuffer(StrUtils.strToBuf(body));
        }
    }

    getSender(): string | null {
        return this._sender;
    }

    getTid(): string {
        return this._tid;
    }

    getTag(): string | null {
        return this._tag;
    }

    getBody(): Buffer {
        return this._body;
    }

    getScheduled(): Date | null {
        return this._scheduled;
    }

    getExpiration(): Date | null {
        return this._expiration;
    }

    isTransaction(): boolean {
        return this._transaction != null;
    }

    isSequence(): boolean {
        return this._sequence;
    }

    getSequenceSharding(): string | null {
        return this._sequenceSharding;
    }

    getQos(): number {
        return this._qos;
    }

    tag(tag: string): MqMessage {
        this._tag = tag;
        return this;
    }

    asJson(): MqMessage {
        this.attr("Content-Type", "application/json");
        return this;
    }

    scheduled(scheduled: Date): MqMessage {
        this._scheduled = scheduled;
        return this;
    }


    expiration(expiration: Date): MqMessage {
        this._expiration = expiration;
        return this;
    }

    sequence(sequence: boolean, sharding?: string): MqMessage {
        this._sequence = sequence;
        if (sharding) {
            this._sequenceSharding = (sequence ? sharding : null);
        }
        return this;
    }

    transaction(transaction: MqTransaction): MqMessage {
        if (transaction) {
            this._transaction = transaction;
            transaction.binding(this);
        }

        return this;
    }

    getTmid(): string | null {
        if (this._transaction == null) {
            return null;
        } else {
            return this._transaction.tmid();
        }
    }

    internalSender(sender: string): MqMessage {
        this._sender = sender;
        return this;
    }

    qos(qos: number): MqMessage {
        this._qos = qos;
        return this;
    }

    getAttr(name: string): string | null {
        let tmp = this._attrMap.get(name);
        return tmp ? tmp : null;
    }

    getAttrMap(): Map<string, string> {
        return this._attrMap;
    }

    attr(name: string, value: string): MqMessage {
        this._attrMap.set(name, value);
        return this;
    }
}
