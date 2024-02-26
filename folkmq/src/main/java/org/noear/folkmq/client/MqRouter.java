package org.noear.folkmq.client;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * 消息路由器
 *
 * @author noear
 * @since 1.3
 */
public class MqRouter implements MqConsumeHandler {
    private final Function<MqMessageReceived, String> routeHandler;
    private MqConsumeHandler consumeHandler;
    private Map<String, MqConsumeHandler> mappingMap = new HashMap<>();

    public MqRouter(Function<MqMessageReceived, String> routeHandler) {
        this.routeHandler = routeHandler;
    }

    public MqRouter on(String mapping, MqConsumeHandler consumeHandler) {
        mappingMap.put(mapping, consumeHandler);
        return this;
    }

    public MqRouter onConsume(MqConsumeHandler consumeHandler) {
        this.consumeHandler = consumeHandler;
        return this;
    }

    @Override
    public void consume(MqMessageReceived message) throws Exception {
        if (consumeHandler != null) {
            consumeHandler.consume(message);
        }

        String mapping = routeHandler.apply(message);
        MqConsumeHandler handler = mappingMap.get(mapping);
        if (handler != null) {
            handler.consume(message);
        }
    }
}