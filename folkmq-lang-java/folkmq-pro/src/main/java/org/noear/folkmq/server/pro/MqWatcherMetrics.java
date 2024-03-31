package org.noear.folkmq.server.pro;

import io.micrometer.core.instrument.Metrics;
import org.noear.folkmq.server.MqMessageHolder;
import org.noear.folkmq.server.MqWatcherDefault;
import org.noear.socketd.transport.core.Message;

/**
 * 消息观察者 - 度量（做监控）
 *
 * @author noear
 * @since 1.0
 */
public class MqWatcherMetrics extends MqWatcherDefault {
    @Override
    public void onPublish(Message message) {
        Metrics.counter("folkmq.publish.count").increment();
    }

    @Override
    public void onAcknowledge(String topic, String consumerGroup, MqMessageHolder messageHolder, boolean isOk) {
        if (isOk) {
            Metrics.counter("folkmq.consume.count").increment();
        }
    }
}
