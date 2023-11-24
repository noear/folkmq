package org.noear.folkmq.client;

import org.noear.folkmq.MqConstants;
import org.noear.socketd.SocketD;
import org.noear.socketd.transport.core.Entity;
import org.noear.socketd.transport.core.Message;
import org.noear.socketd.transport.core.Session;
import org.noear.socketd.transport.core.entity.StringEntity;
import org.noear.socketd.transport.core.listener.BuilderListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 客户端
 *
 * @author noear
 * @since 1.0
 */
public class MqClientImpl extends BuilderListener implements MqClientInternal {
    private static final Logger log = LoggerFactory.getLogger(MqClientImpl.class);

    private String serverUrl;
    private Session session;
    private Map<String, MqConsumerHandler> subscribeMap = new HashMap<>();
    private boolean autoAck = true;

    public MqClientImpl(String serverUrl) throws Exception {
        this.serverUrl = serverUrl.replace("folkmq://", "sd:tcp://");

        this.session = SocketD.createClient(this.serverUrl)
                .listen(this)
                .open();

        //接收派发指令
        on(MqConstants.MQ_CMD_DISTRIBUTE, (s, m) -> {
            String topic = m.meta(MqConstants.MQ_TOPIC);
            try {
                onDistribute(topic, m);

                //是否自动ACK
                if(autoAck){
                    acknowledge(m, true);
                }
            } catch (Exception e) {
                acknowledge(m, false);
            }
        });
    }

    /**
     * 订阅主题
     */
    @Override
    public void subscribe(String topic, MqSubscription subscription) throws IOException {
        //支持Qos1
        subscribeMap.put(topic, subscription.getHandler());

        Entity entity = new StringEntity("")
                .meta(MqConstants.MQ_TOPIC, topic)
                .meta(MqConstants.MQ_USER, subscription.getUser());

        session.sendAndRequest(MqConstants.MQ_CMD_SUBSCRIBE, entity);
    }

    /**
     * 发布消息
     */
    @Override
    public CompletableFuture<?> publish(String topic, String message) throws IOException {
        return publish(topic, message, null);
    }

    @Override
    public CompletableFuture<?> publish(String topic, String message, Date scheduled) throws IOException {
        //支持Qos1
        StringEntity entity = new StringEntity(message);
        entity.meta(MqConstants.MQ_TOPIC, topic);
        if (scheduled == null) {
            entity.meta(MqConstants.MQ_SCHEDULED, "0");
        } else {
            entity.meta(MqConstants.MQ_SCHEDULED, String.valueOf(scheduled.getTime()));
        }

        CompletableFuture<?> future = new CompletableFuture<>();
        session.sendAndSubscribe(MqConstants.MQ_CMD_PUBLISH, entity, r -> {
            future.complete(null);
        });

        return future;
    }


    @Override
    public MqClient autoAck(boolean auto) {
        this.autoAck = auto;
        return this;
    }

    /**
     * 消费确认
     */
    @Override
    public void acknowledge(Message message, boolean isOk) throws IOException {
        session.replyEnd(message, new StringEntity("")
                .meta(MqConstants.MQ_ACK, isOk ? "1" : "0"));
    }


    /**
     * 当派发时
     */
    private void onDistribute(String topic, Message message) throws IOException {
        MqConsumerHandler handler = subscribeMap.get(topic);

        if (handler != null) {
            handler.handle(topic, new MqMessageImpl(this, message));
        }
    }

    /**
     * 当失败时
     */
    @Override
    public void onError(Session session, Throwable error) {
        super.onError(session, error);

        if (log.isWarnEnabled()) {
            log.warn("{}", error);
        }
    }
}
