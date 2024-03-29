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
    private final Function<MqMessageReceived, String> mappingHandler;
    private final Map<String, MqConsumeHandler> mappingMap = new HashMap<>();
    private MqConsumeHandler consumeHandler;

    public MqRouter(Function<MqMessageReceived, String> mappingHandler) {
        this.mappingHandler = mappingHandler;
    }

    /**
     * 添加映射处理
     */
    public MqRouter doOn(String mapping, MqConsumeHandler consumeHandler) {
        mappingMap.put(mapping, consumeHandler);
        return this;
    }

    /**
     * 添加消费处理
     */
    public MqRouter doOnConsume(MqConsumeHandler consumeHandler) {
        this.consumeHandler = consumeHandler;
        return this;
    }

    /**
     * 消费
     */
    @Override
    public void consume(MqMessageReceived message) throws Exception {
        if (consumeHandler != null) {
            consumeHandler.consume(message);
        }

        String mapping = mappingHandler.apply(message);
        MqConsumeHandler handler = mappingMap.get(mapping);
        if (handler != null) {
            handler.consume(message);
        }
    }
}