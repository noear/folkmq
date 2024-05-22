import {Entity, EntityDefault, StringEntity} from "@noear/socket.d/transport/core/Entity";
import {MqMessage} from "../client/MqMessage";
import {Message} from "@noear/socket.d/transport/core/Message";

/**
 * 消息元信息分析器
 *
 * @author noear
 * @since 1.2
 */
export interface MqMetasResolver {
    /**
     * 版本号
     * */
    version(): number;

    /**
     * 获取发送人
     * */
    getSender(m: Entity): string;

    /**
     * 获取主建
     */
    getKey(m: Entity): string;

    /**
     * 获取标签
     * */
    getTag(m: Entity): string;

    /**
     * 获取主题
     */
    getTopic(m: Entity): string;

    /**
     * 获取消费者组
     */
    getConsumerGroup(m: Entity): string;

    /**
     * 设置消费者组
     */
    setConsumerGroup(m: Entity, consumerGroup: string);

    /**
     * 获取质量等级（0或1）
     */
    getQos(m: Entity): number;

    /**
     * 获取派发次数
     */
    getTimes(m: Entity): number;

    /**
     * 设置派发次数
     */
    setTimes(m: Entity, times: number);

    /**
     * 获取过期时间
     */
    getExpiration(m: Entity): number;

    /**
     * 设置过期时间
     * */
    setExpiration(m: Entity, expiration: number);

    /**
     * 获取定时时间
     */
    getScheduled(m: Entity): number;

    /**
     * 设置定时时间
     */
    setScheduled(m: Entity, scheduled: number);

    /**
     * 是否有序
     */
    isSequence(m: Entity): boolean;

    /**
     * 是否广播
     * */
    isBroadcast(m: Entity): boolean;

    /**
     * 是否事务
     */
    isTransaction(m: Entity): boolean;

    /**
     * 设置事务
     * */
    setTransaction(m: Entity, isTransaction: boolean);

    /**
     * 发布实体构建
     *
     * @param topic   主题
     * @param message 消息
     */
    publishEntityBuild(topic: string, message: MqMessage): EntityDefault;

    /**
     * 路由消息构建
     *
     * @param topic   主题
     * @param message 消息
     */
    routingMessageBuild(topic: string, message: MqMessage): Message;
}