package org.noear.folkmq.server.pro;

import org.noear.folkmq.common.MqConstants;
import org.noear.folkmq.server.MqWatcherDefault;
import org.noear.socketd.transport.core.Message;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 消息观察者 - 度量（做监控）
 *
 * @author noear
 * @since 1.0
 */
public class MqWatcherMetrics extends MqWatcherDefault {
    private AtomicLong messageTotal = new AtomicLong();
    private Map<String, AtomicLong> topicMessageTotal = new ConcurrentHashMap<>();

    public AtomicLong getMessageTotal() {
        return messageTotal;
    }

    public Map<String, AtomicLong> getTopicMessageTotal() {
        return topicMessageTotal;
    }

    @Override
    public void onPublish(Message message) {
        String topic = message.meta(MqConstants.MQ_META_TOPIC);

        messageTotal.incrementAndGet();
        topicMessageTotal.computeIfAbsent(topic, (s) -> new AtomicLong()).incrementAndGet();
    }
}
