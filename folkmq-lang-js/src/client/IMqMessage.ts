import {Message} from "@noear/socket.d/transport/core/Message";
import {Session} from "@noear/socket.d/transport/core/Session";
import {StrUtils} from "@noear/socket.d/utils/StrUtils";
import {MqClientInternal} from "./MqClient";
import {MqConstants} from "../common/MqConstants";

export interface IMqMessage {
    /**
     * 事务ID
     */
    getTid(): string;

    /**
     * 内容
     */
    getContent(): string;

    /**
     * 计划时间 //::long
     */
    getScheduled(): Date | null;

    /**
     * 质量等级（0 或 1） //:int
     */
    getQos(): number;
}



export class MqMessage implements IMqMessage {
    private _tid: string;
    private _content: string;
    private _scheduled: Date;
    private _qos: number = 1;

    constructor(content: string) {
        this._tid = StrUtils.guid();
        this._content = content;
    }

    scheduled(scheduled: Date): MqMessage {
        this._scheduled = scheduled;
        return this;
    }

    qos(qos: number): MqMessage {
        this._qos = qos;
        return this;
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

    getQos(): number {
        return this._qos;
    }
}

export interface MqMessageReceived {
    /**
     * 主题
     */
    getTopic(): string;

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
    private readonly _content: string;
    private readonly _qos: number;
    private readonly _times: number;

    constructor(clientInternal: MqClientInternal, session: Session, from: Message) {
        this._clientInternal = clientInternal;
        this._session = session;
        this._from = from;

        this._tid = from.metaOrDefault(MqConstants.MQ_META_TID, "");
        this._topic = from.metaOrDefault(MqConstants.MQ_META_TOPIC, "");
        this._content = from.dataAsString();

        this._qos = parseInt(from.metaOrDefault(MqConstants.MQ_META_QOS, "1"));
        this._times = parseInt(from.metaOrDefault(MqConstants.MQ_META_TIMES, "0"));
    }

    /**
     * 事务ID
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
            ", content='" + this._content + '\'' +
            ", qos=" + this._qos +
            ", times=" + this._times +
            '}';
    }
}