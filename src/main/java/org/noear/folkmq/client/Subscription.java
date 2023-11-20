package org.noear.folkmq.client;

/**
 * 订阅者
 *
 * @author noear
 * @since 1.0
 */
public class Subscription {
    private String identity;
    private MqConsumerHandler handler;

    public String getIdentity() {
        return identity;
    }

    public MqConsumerHandler getHandler() {
        return handler;
    }

    public Subscription(String identity, MqConsumerHandler handler) {
        this.identity = identity;
        this.handler = handler;
    }
}
