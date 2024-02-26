import {Message} from "@noear/socket.d/transport/core/Message";
import {Session} from "@noear/socket.d/transport/core/Session";
import {StrUtils} from "@noear/socket.d/utils/StrUtils";
import {MqClientInternal} from "./MqClient";
import {MqConstants} from "../common/MqConstants";

export interface IMqMessage {
    /**
     * 跟踪ID
     */
    getTid(): string;

    /**
     * 内容
     */
    getContent(): string;

    /**
     * 过期时间 //::long
     */
    getExpiration(): Date | null;

    /**
     * 质量等级（0 或 1） //:int
     */
    getQos(): number;
}



export class MqMessage implements IMqMessage {
    private readonly _tid: string;
    private readonly _content: string;
    private _scheduled: Date | null = null;
    private _expiration: Date | null = null;
    private _sequence: boolean = false;
    private _qos: number = 1;

    constructor(content: string) {
        this._tid = StrUtils.guid();
        this._content = content;
    }

    getTid(): string {
        return this._tid;
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

    isSequence(): boolean {
        return this._sequence;
    }

    getQos(): number {
        return this._qos;
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

    qos(qos: number): MqMessage {
        this._qos = qos;
        return this;
    }
}

export interface MqMessageReceived {
    /**
     * 主题
     */
    getTopic(): string;

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
}

export class MqMessageReceivedImpl implements MqMessageReceived {
    private _clientInternal: MqClientInternal;
    private _from: Message;
    private _session: Session;

    private readonly _tid: string;
    private readonly _topic: string;
    private readonly _consumerGroup: string;
    private readonly _content: string;
    private readonly _expiration: Date | null;
    private readonly _qos: number;
    private readonly _times: number;

    constructor(clientInternal: MqClientInternal, session: Session, from: Message) {
        this._clientInternal = clientInternal;
        this._session = session;
        this._from = from;

        this._tid = from.metaOrDefault(MqConstants.MQ_META_TID, "");
        this._topic = from.metaOrDefault(MqConstants.MQ_META_TOPIC, "");
        this._consumerGroup = from.metaOrDefault(MqConstants.MQ_META_CONSUMER_GROUP, "");
        this._content = from.dataAsString();

        this._qos = parseInt(from.metaOrDefault(MqConstants.MQ_META_QOS, "1"));
        this._times = parseInt(from.metaOrDefault(MqConstants.MQ_META_TIMES, "0"));

        let expirationL: number = parseInt(from.metaOrDefault(MqConstants.MQ_META_EXPIRATION, "0"));
        if (expirationL == 0) {
            this._expiration = null;
        } else {
            this._expiration = new Date(expirationL);
        }
    }

    /**
     * 跟踪ID
     */
    getTid(): string {
        return this._tid;
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

    /**
     * 内容
     */
    getContent(): string {
        return this._content;
    }


    /**
     * 计划时间
     */
    getScheduled(): Date | null {
        return null;
    }

    /**
     * 质量等级（0 或 1）
     */
    getQos(): number {
        return this._qos;
    }

    /**
     * 过期时间
     * */
    getExpiration(): Date | null {
        return this._expiration;
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
        //发送“回执”，向服务端反馈消费情况
        this._clientInternal.acknowledge(this._session, this._from, this, isOk);
    }

    toString(): string {
        return "MqMessageReceived{" +
            "tid='" + this._tid + '\'' +
            ", topic='" + this._topic + '\'' +
            ", consumerGroup='" + this._consumerGroup + '\'' +
            ", content='" + this._content + '\'' +
            ", qos=" + this._qos +
            ", times=" + this._times +
            '}';
    }
}