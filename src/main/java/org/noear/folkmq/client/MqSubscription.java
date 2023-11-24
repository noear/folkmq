package org.noear.folkmq.client;

/**
 * 订阅者
 *
 * @author noear
 * @since 1.0
 */
public class MqSubscription {
    private String user;
    private MqConsumerHandler handler;

    /**
     * 用户（可以是实例 IP，或集群 Name）
     */
    public String getUser() {
        return user;
    }

    /**
     * 消息处理器
     */
    public MqConsumerHandler getHandler() {
        return handler;
    }

    /**
     * @param user    用户（可以是实例 IP，或集群 Name）
     * @param handler 消息处理器
     */
    public MqSubscription(String user, MqConsumerHandler handler) {
        this.user = user;
        this.handler = handler;
    }
}
