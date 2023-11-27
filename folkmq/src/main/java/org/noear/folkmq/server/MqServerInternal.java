package org.noear.folkmq.server;

import org.noear.socketd.transport.core.Message;
import org.noear.socketd.transport.core.Session;

import java.util.Map;
import java.util.Set;

/**
 * 消息服务端内部接口
 *
 * @author noear
 * @since 1.0
 */
public interface MqServerInternal extends MqServer {
    /**
     * 获取订阅关系表(topic=>[topicConsumer])
     */
    Map<String, Set<String>> getSubscribeMap();

    /**
     * 获取主题消息者队列表(topicConsumer=>MqTopicConsumerQueue)
     */
    Map<String, MqTopicConsumerQueue> getTopicConsumerMap();

    /**
     * 执行订阅
     */
    void subscribeDo(String topic, String consumer, Session session);

    /**
     * 执行取消订阅
     */
    void unsubscribeDo(String topic, String consumer, Session session);

    /**
     * 执行交换
     */
    void exchangeDo(Message message);

    /**
     * 执行回执
     */
    void acknowledgeDo(Message message);
}
