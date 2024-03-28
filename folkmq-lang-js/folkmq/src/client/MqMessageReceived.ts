import {MqClientInternal} from "./MqClient";
import {Message} from "@noear/socket.d/transport/core/Message";
import {Session} from "@noear/socket.d/transport/core/Session";
import {MqConstants} from "../common/MqConstants";
import {Entity} from "@noear/socket.d/transport/core/Entity";
import {MqUtils} from "../common/MqUtils";
import {MqMessageBase} from "./MqMessage";

export interface MqMessageReceived extends MqMessageBase{
    /**
     * 主题
     */
    getTopic(): string;

    /**
     * 内容
     *
     * @deprecated 1.4
     * */
    getContent(): string;

    /**
     * 内容
     */
    getBodyAsString(): string;

    /**
     * 消费者组
     */
    getConsumerGroup(): string;

    /**
     * 已派发次数
     */
    getTimes(): number;

    /**
     * 回执
     */
    acknowledge(isOk: boolean);

    /**
     * 响应
     * */
    response(entity: Entity | null);
}

export class MqMessageReceivedImpl implements MqMessageReceived {
    private _clientInternal: MqClientInternal;
    private _source: Message;
    private _session: Session;

    private readonly _sender: string;
    private readonly _tid: string;
    private readonly _tag: string;
    private readonly _topic: string;
    private readonly _consumerGroup: string;
    private readonly _expiration: Date | null;
    private readonly _sequence: boolean;
    private readonly _transaction: boolean;
    private readonly _qos: number;
    private readonly _times: number;

    constructor(clientInternal: MqClientInternal, session: Session, source: Message) {
        this._clientInternal = clientInternal;
        this._session = session;
        this._source = source;

        let mr = MqUtils.getOf(source);

        this._sender = mr.getSender(source);

        this._tid = mr.getTid(source);
        this._tag = mr.getTag(source);
        this._topic = mr.getTopic(source);
        this._consumerGroup = mr.getConsumerGroup(source);

        this._qos = mr.getQos(source);
        this._times = mr.getTimes(source);
        this._sequence = mr.isSequence(source);
        this._transaction = mr.isTransaction(source);

        let expirationL = mr.getExpiration(source);
        if (expirationL == 0) {
            this._expiration = null;
        } else {
            this._expiration = new Date(expirationL);
        }
    }

    /**
     * 获取消息源
     */
    getSource() {
        return this._source;
    }

    getSender(): string {
        return this._sender;
    }

    /**
     * 跟踪ID
     */
    getTid(): string {
        return this._tid;
    }

    /**
     * 标签
     */
    getTag(): string {
        return this._tag;
    }

    /**
     * 主题
     */
    getTopic(): string {
        return this._topic;
    }

    /**
     * 消费者组
     * */
    getConsumerGroup(): string {
        return this._consumerGroup;
    }

    getContent(): string {
        return this.getBodyAsString();
    }

    getBody(): ArrayBuffer {
        return this._source.data().getArray()!;
    }

    /**
     * 内容
     */
    getBodyAsString(): string {
        return this._source.dataAsString();
    }

    /**
     * 质量等级（0 或 1）
     */
    getQos(): number {
        return this._qos;
    }

    getAttr( name:string):string|null {
        return this._source.meta(MqConstants.MQ_ATTR_PREFIX + name);
    }

    /**
     * 过期时间
     * */
    getExpiration(): Date | null {
        return this._expiration;
    }

    /**
     * 是否事务
     */
    isTransaction(): boolean {
        return this._transaction;
    }

    /**
     * 是否有序
     */
    isSequence(): boolean {
        return this._sequence;
    }

    /**
     * 已派发次数
     */
    getTimes(): number {
        return this._times;
    }

    /**
     * 回执
     */
    acknowledge(isOk: boolean) {
        this._clientInternal.reply(this._session, this._source, this, isOk, null);
    }

    response(entity: Entity | null) {
        this._clientInternal.reply(this._session, this._source, this, true, entity);
    }

    toString(): string {
        return "MqMessageReceived{" +
            "tid='" + this._tid + '\'' +
            ", tag='" + this._tag + '\'' +
            ", topic='" + this._topic + '\'' +
            ", body='" + this.getBodyAsString() + '\'' +
            '}';
    }
}