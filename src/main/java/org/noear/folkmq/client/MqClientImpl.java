package org.noear.folkmq.client;

import org.noear.folkmq.MqConstants;
import org.noear.socketd.SocketD;
import org.noear.socketd.transport.core.Entity;
import org.noear.socketd.transport.core.Message;
import org.noear.socketd.transport.core.Session;
import org.noear.socketd.transport.core.entity.StringEntity;
import org.noear.socketd.transport.core.listener.BuilderListener;
import org.noear.socketd.transport.core.listener.SimpleListener;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @author noear
 * @since 1.0
 */
public class MqClientImpl extends BuilderListener implements MqClient {
    private String serverUrl;
    private Session session;
    private Map<String, MqConsumerHandler> subscribeMap = new HashMap<>();

    public MqClientImpl(String serverUrl) throws Exception {
        this.serverUrl = serverUrl.replace("folkmq://", "sd:tcp://");

        this.session = SocketD.createClient(this.serverUrl)
                .listen(this)
                .open();

        on(MqConstants.MQ_CMD_DISTRIBUTE, (s, m) -> {
            String topic = m.meta(MqConstants.MQ_TOPIC);
            onDistribute(topic, m.dataAsString());
        });
    }

    /**
     * 订阅
     */
    @Override
    public CompletableFuture<?> subscribe(String topic, Subscription subscription) throws IOException {
        //支持Qos1
        subscribeMap.put(topic, subscription.getHandler());

        Entity entity = new StringEntity("")
                .meta(MqConstants.MQ_TOPIC, topic)
                .meta(MqConstants.MQ_IDENTITY,subscription.getIdentity());

        CompletableFuture<?> future = new CompletableFuture<>();
        session.sendAndSubscribe(MqConstants.MQ_CMD_SUBSCRIBE, entity, (r)->{
            future.complete(null);
        });

        return future;
    }

    /**
     * 发布
     */
    @Override
    public CompletableFuture<?> publish(String topic, String message) throws IOException {
        //支持Qos1
        CompletableFuture<?> future = new CompletableFuture<>();
        session.sendAndSubscribe(MqConstants.MQ_CMD_PUBLISH, new StringEntity(message).meta(MqConstants.MQ_TOPIC, topic), r -> {
            future.complete(null);
        });

        return future;
    }

    /**
     * 当派发时
     */
    private void onDistribute(String topic, String message) throws IOException {
        MqConsumerHandler handler = subscribeMap.get(topic);

        if (handler != null) {
            handler.handle(topic, message);
        }
    }
}
