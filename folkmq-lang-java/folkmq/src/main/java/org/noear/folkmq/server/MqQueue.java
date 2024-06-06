package org.noear.folkmq.server;

import org.noear.socketd.transport.core.Session;

import java.util.Collection;
import java.util.concurrent.atomic.LongAdder;

/**
 * 队列（服务端给每对 [主题#消费者组] 安排一个队列，队列内通过轮询负载平衡派发）
 *
 * @author noear
 * @since 1.0
 */
public interface MqQueue {
    /**
     * 是否为事务缓存队列
     * */
    boolean isTransaction();
    /**
     * 获取主题
     */
    String getTopic();

    /**
     * 获取消费组
     */
    String getConsumerGroup();

    /**
     * 获取队列名
     */
    String getQueueName();

    /**
     * 添加消费者会话
     */
    void sessionAdd(Session session);

    /**
     * 移除消费者会话
     */
    void sessionRemove(Session session);

    /**
     * 获取所有消息会话
     */
    Collection<Session> sessionAll();

    /**
     * 消费者会话数量
     */
    int sessionCount();

    /**
     * 添加消息
     */
    void add(MqMessageHolder messageHolder);

    /**
     * 移除消息
     */
    void removeAt(String key);

    /**
     * 确认消息
     * */
    void affirmAt(String key, boolean isRollback);

    /**
     * 派发消息
     */
    boolean distribute();

    /**
     * 强制清空
     */
    void forceClear();

    /**
     * 强制派发
     */
    void forceDistribute(int times, int count);

    /**
     * 消息总量
     */
    int messageTotal();

    /**
     * 消息总量2（用作校验）
     */
    int messageTotal2();

    /**
     * 消息变更计数
     * */
    LongAdder messageChangedCount();

    /**
     * 关闭
     */
    void close();
}