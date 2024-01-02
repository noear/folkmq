import * as SocketD from "socketd";
import {Message} from "transport/core/Message";
import {Session} from "transport/core/Session";
import {IoConsumer} from "transport/core/Typealias";
import {ClientConfig} from "transport/client/ClientConfig";
import {StrUtils} from "utils/StrUtils";

export class MqConstants{
    /**
     * 元信息：消息事务Id
     */
    static MQ_META_TID = "mq.tid";
    /**
     * 元信息：消息主题
     */
    static MQ_META_TOPIC = "mq.topic";
    /**
     * 元信息：消息调度时间
     */
    static MQ_META_SCHEDULED = "mq.scheduled";
    /**
     * 元信息：消息质量等级
     */
    static MQ_META_QOS = "mq.qos";
    /**
     * 元信息：消费者组
     */
    static MQ_META_CONSUMER_GROUP = "mq.consumer"; //此处不改动，算历史痕迹。保持向下兼容
    /**
     * 元信息：派发次数
     */
    static MQ_META_TIMES = "mq.times";
    /**
     * 元信息：消费回执
     */
    static MQ_META_ACK = "mq.ack";
    /**
     * 元信息：执行确认
     */
    static MQ_META_CONFIRM = "mq.confirm";
    /**
     * 元信息：批量处理
     */
    static MQ_META_BATCH = "mq.batch";

    /**
     * 事件：订阅
     */
    static MQ_EVENT_SUBSCRIBE = "mq.event.subscribe";
    /**
     * 事件：取消订阅
     */
    static MQ_EVENT_UNSUBSCRIBE = "mq.event.unsubscribe";
    /**
     * 事件：发布
     */
    static MQ_EVENT_PUBLISH = "mq.event.publish";
    /**
     * 事件：取消发布
     */
    static MQ_EVENT_UNPUBLISH = "mq.event.unpublish";
    /**
     * 事件：派发
     */
    static MQ_EVENT_DISTRIBUTE = "mq.event.distribute";
    /**
     * 事件：保存快照
     */
    static MQ_EVENT_SAVE = "mq.event.save";

    /**
     * 事件：加入集群
     * */
    static MQ_EVENT_JOIN = "mq.event.join";

    /**
     * 管理视图-队列
     */
    static ADMIN_VIEW_QUEUE = "admin.view.queue";

    /**
     * 连接参数：ak
     */
    static PARAM_ACCESS_KEY = "ak";
    /**
     * 连接参数: sk
     */
    static PARAM_ACCESS_SECRET_KEY = "sk";

    /**
     * 主题与消息者间隔符
     */
    static SEPARATOR_TOPIC_CONSUMER_GROUP = "#";

    /**
     * 经理人服务
     */
    static BROKER_AT_SERVER = "folkmq-server";

    /**
     * 经理人所有服务
     */
    static BROKER_AT_SERVER_ALL = "folkmq-server*";

    /**
     * 最大分片大小（1m）
     */
    static MAX_FRAGMENT_SIZE = 1024 * 1024;
}

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

export interface MqClient {
    /**
     * 连接
     */
    connect(): Promise<MqClient>;

    /**
     * 断开连接
     */
    disconnect();

    config(configHandler: IoConsumer<ClientConfig>): MqClient;

    /**
     * 客户端配置
     */

    //config( configHandler: ClientConfigHandler):MqClient;

    /**
     * 自动回执
     *
     * @param auto 自动（默认为 true）
     */
    autoAcknowledge(auto: boolean): MqClient;

    /**
     * 订阅主题
     *
     * @param topic           主题
     * @param consumerGroup   消费者组
     * @param consumerHandler 消费处理
     */
    subscribe(topic: string, consumerGroup: string, consumerHandler: MqConsumeHandler): Promise<boolean>;

    /**
     * 取消订阅主题
     *
     * @param topic         主题
     * @param consumerGroup 消费者组
     */
    unsubscribe(topic: string, consumerGroup: string): Promise<boolean>;

    /**
     * 同步发布消息
     *
     * @param topic   主题
     * @param message 消息
     */
    publish(topic: string, message: IMqMessage): Promise<boolean>;

    /**
     * 取消发布
     *
     * @param topic 主题
     * @param tid   事务id
     */
    unpublish(topic: string, tid: string): Promise<boolean>;
}

export interface MqClientInternal {
    /**
     * 消费回执
     *
     * @param session 会话
     * @param from    来源消息
     * @param message 收到的消息
     * @param isOk    回执
     */
    acknowledge(session: Session, from: Message, message: MqMessageReceivedImpl, isOk: boolean);
}

export interface MqConsumeHandler {
    /**
     * 消费
     *
     * @param message 收到的消息
     */
    consume(message: MqMessageReceived);
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

    scheduled(scheduled: Date): MqMessage {
        this._scheduled = scheduled;
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
     * 已派发次数
     */
    getTimes(): number;

    /**
     * 回执
     */
    acknowledge(isOk: boolean);
}

class MqMessageReceivedImpl implements MqMessageReceived {
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

class MqSubscription implements MqConsumeHandler {
    private readonly _topic: string;
    private readonly _consumerGroup: string;
    private readonly _queueName: string;
    private readonly _consumeHandler: MqConsumeHandler;


    /**
     * 主题
     */
    getTopic(): string {
        return this._topic;
    }

    /**
     * 消费者组
     */
    getConsumerGroup(): string {
        return this._consumerGroup;
    }

    /**
     * 相关队列名
     */
    pgetQueueName(): string {
        return this._queueName;
    }

    /**
     * 消费处理器
     */
    getConsumeHandler(): MqConsumeHandler {
        return this._consumeHandler;
    }


    /**
     * @param topic          主题
     * @param consumerGroup  消费者组
     * @param consumeHandler 消费处理器
     */
    constructor(topic: string, consumerGroup: string, consumeHandler: MqConsumeHandler) {
        this._topic = topic;
        this._consumerGroup = consumerGroup;
        this._queueName = topic + '#' + consumerGroup;
        this._consumeHandler = consumeHandler;
    }

    /**
     * 消费
     *
     * @param message 收到的消息
     */
    consume(message: MqMessageReceived) {
        this._consumeHandler.consume(message);
    }
}

