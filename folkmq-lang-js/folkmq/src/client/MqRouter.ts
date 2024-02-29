import {IoConsumer, IoFunction} from "@noear/socket.d/transport/core/Typealias";
import {MqMessageReceived} from "./MqMessageReceived";

/**
 * 消息路由器
 *
 * @author noear
 * @since 1.3
 */
export class MqRouter {
    private readonly _mappingHandler: IoFunction<MqMessageReceived, string>;
    private readonly _mappingMap = new Map<string, IoConsumer<MqMessageReceived>>();
    private _consumeHandler: IoConsumer<MqMessageReceived>;

    constructor(mappingHandler: IoFunction<MqMessageReceived, string>) {
        this._mappingHandler = mappingHandler;
    }

    /**
     * 添加映射处理
     */
    doOn(mapping: string, consumeHandler: IoConsumer<MqMessageReceived>): MqRouter {
        this._mappingMap.set(mapping, consumeHandler);
        return this;
    }

    /**
     * 添加消费处理
     */
    doOnConsume(consumeHandler: IoConsumer<MqMessageReceived>): MqRouter {
        this._consumeHandler = consumeHandler;
        return this;
    }

    /**
     * 消费
     */
    consume(message: MqMessageReceived) {
        if (this._consumeHandler != null) {
            this._consumeHandler(message);
        }

        let mapping = this._mappingHandler(message);
        let handler = this._mappingMap.get(mapping);
        if (handler != null) {
            handler(message);
        }
    }
}