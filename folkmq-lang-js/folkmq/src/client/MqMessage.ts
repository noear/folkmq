import {StrUtils} from "@noear/socket.d/utils/StrUtils";
import {MqTransaction} from "./MqTransaction";

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
     */
    getContent(): string;

    /**
     * 过期时间 //::long
     */
    getExpiration(): Date | null;

    /**
     * 是否为事务
     */
    isTransaction(): boolean;

    /**
     * 是否为有序
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
    private readonly _content: string;

    private _sender: string | null = null;
    private _tag: string | null = null;
    private _scheduled: Date | null = null;
    private _expiration: Date | null = null;
    private _sequence: boolean = false;
    private _qos: number = 1;

    protected _attrMap = new Map<string, string>;
    protected _transaction: MqTransaction;

    constructor(content: string) {
        this._tid = StrUtils.guid();
        this._content = content;
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

    getContent(): string {
        return this._content;
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

    sequence(sequence: boolean): MqMessage {
        this._sequence = sequence;
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
