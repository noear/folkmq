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
     * 获取订阅关系表
     * */
    Map<String, Set<String>> getSubscribeMap();

    /**
     * 获取消息队列表
     * */
    Map<String, MqConsumerQueue> getConsumerMap();

    /**
     * 执行订阅
     */
    void subscribeDo(String topic, String consumer, Session session);

    /**
     * 执行交换
     */
    void exchangeDo(String topic, Message message);
}
