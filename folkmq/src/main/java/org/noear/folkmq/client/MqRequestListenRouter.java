package org.noear.folkmq.client;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 请求监听路由器
 *
 * @author noear
 * @since 1.2
 */
public class MqRequestListenRouter implements MqRequestListener {
    private MqRequestListener requestHandler;
    private Map<String, MqRequestListener> doOnMap = new ConcurrentHashMap<>();

    /**
     * 配置所有请求处理
     */
    public MqRequestListenRouter doOnRequest(MqRequestListener requestHandler) {
        this.requestHandler = requestHandler;
        return this;
    }

    /**
     * 配置主题请求处理
     */
    public MqRequestListenRouter doOn(String topic, MqRequestListener requestHandler) {
        doOnMap.put(topic, requestHandler);
        return this;
    }

    @Override
    public void onRequest(MqMessageReceived message) throws Exception {
        if (requestHandler != null) {
            requestHandler.onRequest(message);
        }

        MqRequestListener topicHanler = doOnMap.get(message.getTopic());
        if (topicHanler != null) {
            topicHanler.onRequest(message);
        }
    }
}
