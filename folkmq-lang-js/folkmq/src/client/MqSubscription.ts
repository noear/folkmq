
import {IoConsumer} from "@noear/socket.d/transport/core/Typealias";
import {MqMessageReceived} from "./MqMessageReceived";
import {MqConstants} from "../common/MqConstants";

export class MqSubscription {
    private readonly _topic: string;
    private readonly _consumerGroup: string;
    private readonly _queueName: string;
    private readonly _autoAck: boolean;
    private readonly _consumeHandler: IoConsumer<MqMessageReceived>;

    /**
     * @param topic          主题
     * @param consumerGroup  消费者组
     * @param consumeHandler 消费处理器
     */
    constructor(topic: string, consumerGroup: string, autoAck: boolean, consumeHandler: IoConsumer<MqMessageReceived>) {
        this._topic = topic;
        this._consumerGroup = consumerGroup;
        this._autoAck = autoAck;
        this._queueName = topic + MqConstants.SEPARATOR_TOPIC_CONSUMER_GROUP + consumerGroup;
        this._consumeHandler = consumeHandler;
    }

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

    isAutoAck(): boolean {
        return this._autoAck;
    }

    /**
     * 相关队列名
     */
    getQueueName(): string {
        return this._queueName;
    }

    /**
     * 消费
     *
     * @param message 收到的消息
     */
    consume(message: MqMessageReceived) {
        this._consumeHandler(message);
    }
}