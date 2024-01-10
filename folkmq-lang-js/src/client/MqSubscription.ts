import {MqMessageReceived} from "./IMqMessage";
import {IoConsumer} from "@noear/socket.d/transport/core/Typealias";

export class MqSubscription {
    private readonly _topic: string;
    private readonly _consumerGroup: string;
    private readonly _queueName: string;
    private readonly _consumeHandler: IoConsumer<MqMessageReceived>;


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
    getQueueName(): string {
        return this._queueName;
    }

    /**
     * @param topic          主题
     * @param consumerGroup  消费者组
     * @param consumeHandler 消费处理器
     */
    constructor(topic: string, consumerGroup: string, consumeHandler: IoConsumer<MqMessageReceived>) {
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
        this._consumeHandler(message);
    }
}