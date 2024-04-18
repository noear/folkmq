import {ClientConfig} from "@noear/socket.d/transport/client/ClientConfig";
import {IoConsumer} from "@noear/socket.d/transport/core/Typealias";
import {Session} from "@noear/socket.d/transport/core/Session";
import {Message} from "@noear/socket.d/transport/core/Message";
import {MqMessageReceived, MqMessageReceivedImpl} from "./MqMessageReceived";
import {MqMessage} from "./MqMessage";
import {Entity} from "@noear/socket.d/transport/core/Entity";
import {RequestStream} from "@noear/socket.d/transport/stream/Stream";
import {MqTransaction} from "./MqTransaction";


/**
 * 消息客户端
 *
 * @author noear
 * @since 1.0
 * @since 1.2
 */
export interface MqClient {
    /**
     * 名字（即，默认消费者组）
     */
    name(): string;

    /**
     * 名字取为（即，默认消费者组）
     */
    nameAs(name: string): MqClient;

    /**
     * 命名空间
     */
    namespace(): string;

    /**
     * 命名空间
     * @since 1.4
     */
    namespaceAs(namespace: string): MqClient;

    /**
     * 连接
     */
    connect(): Promise<MqClient>;

    /**
     * 断开连接
     */
    disconnect();

    /**
     * 客户端配置
     */
    config(configHandler: IoConsumer<ClientConfig>): MqClient;

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
    subscribe(topic: string, consumerGroup: string | null, autoAck: boolean | null, consumerHandler: IoConsumer<MqMessageReceived>);

    /**
     * 取消订阅主题
     *
     * @param topic         主题
     * @param consumerGroup 消费者组
     */
    unsubscribe(topic: string, consumerGroup: string | null);

    /**
     * 同步发布消息
     *
     * @param topic   主题
     * @param message 消息
     */
    publish(topic: string, message: MqMessage);

    /**
     * 取消发布
     *
     * @param topic 主题
     * @param key   主建
     */
    unpublish(topic: string, key: string);

    /**
     * 监听
     *
     * @param listenHandler 监听处理
     */
    listen(listenHandler: IoConsumer<MqMessageReceived>);

    /**
     * 发送
     *
     * @param message 消息
     * @param toName  发送目标名字
     * @param timeout 超时（单位毫秒）
     */
    send(message: MqMessage, toName: string, timeout?: number): RequestStream | null;

    /**
     * 事务回查
     *
     * @param transactionCheckback 事务回查处理
     */
    transactionCheckback(transactionCheckback: IoConsumer<MqMessageReceived>);

    /**
     * 新建事务
     */
    newTransaction(): MqTransaction;
}

export interface MqClientInternal extends MqClient {
    /**
     * 发布二次提交
     *
     * @param tmid       事务管理id
     * @param keyAry     消息主键集合
     * @param isRollback 是否回滚
     */
    publish2(tmid: string, keyAry: string[], isRollback: boolean);

    /**
     * 消费答复
     *
     * @param session 会话
     * @param from    来源消息
     * @param message 收到的消息
     * @param isOk    回执
     * @param entity  实体
     */
    reply(session: Session, from: Message, message: MqMessageReceivedImpl, isOk: boolean, entity: Entity | null);
}