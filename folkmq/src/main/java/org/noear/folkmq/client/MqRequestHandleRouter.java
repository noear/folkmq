package org.noear.folkmq.client;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 请求处理路由器
 *
 * @author noear
 * @since 1.2
 */
public class MqRequestHandleRouter implements MqRequestHandler {
    private MqRequestHandler requestHandler;
    private Map<String, MqRequestHandler> doOnMap = new ConcurrentHashMap<>();

    /**
     * 配置所有请求处理
     */
    public MqRequestHandleRouter doOnRequest(MqRequestHandler requestHandler) {
        this.requestHandler = requestHandler;
        return this;
    }

    /**
     * 配置主题请求处理
     */
    public MqRequestHandleRouter doOn(String topic, MqRequestHandler requestHandler) {
        doOnMap.put(topic, requestHandler);
        return this;
    }

    @Override
    public void onRequest(MqMessageReceived message) throws Exception {
        if (requestHandler != null) {
            requestHandler.onRequest(message);
        }

        MqRequestHandler topicHanler = doOnMap.get(message.getTopic());
        if (topicHanler != null) {
            topicHanler.onRequest(message);
        }
    }
}
