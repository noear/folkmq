package org.noear.folkmq.client;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 请求响应路由器
 *
 * @author noear
 * @since 1.2
 */
public class MqResponseRouter implements MqResponder {
    private MqResponder requestHandler;
    private Map<String, MqResponder> doOnMap = new ConcurrentHashMap<>();

    /**
     * 配置所有请求处理
     */
    public MqResponseRouter doOnRequest(MqResponder requestHandler) {
        this.requestHandler = requestHandler;
        return this;
    }

    /**
     * 配置主题请求处理
     */
    public MqResponseRouter doOn(String topic, MqResponder requestHandler) {
        doOnMap.put(topic, requestHandler);
        return this;
    }

    @Override
    public void onRequest(MqMessageReceived message) throws Exception {
        if (requestHandler != null) {
            requestHandler.onRequest(message);
        }

        MqResponder topicHanler = doOnMap.get(message.getTopic());
        if (topicHanler != null) {
            topicHanler.onRequest(message);
        }
    }
}
