package org.noear.folkmq.server;

import org.noear.socketd.transport.core.Message;
import org.noear.socketd.transport.core.Session;

/**
 * 消息观察者默认实现
 *
 * @author noear
 * @since 1.0
 */
public class MqWatcherDefault implements MqWatcher {


    @Override
    public void init(MqServiceInternal serverInternal) {

    }

    @Override
    public void onStartBefore() {

    }

    @Override
    public void onStartAfter() {

    }

    @Override
    public void onStopBefore() {

    }

    @Override
    public void onStopAfter() {

    }

    @Override
    public void onSave() {

    }

    @Override
    public void onSubscribe(String topic, String consumerGroup, Session session) {

    }

    @Override
    public void onUnSubscribe(String topic, String consumerGroup, Session session) {

    }

    @Override
    public void onPublish(Message message) {

    }

    @Override
    public void onUnPublish(Message message) {

    }

    @Override
    public void onDistribute(String topic, String consumerGroup, MqMessageHolder messageHolder) {

    }

    @Override
    public void onAcknowledge(String topic, String consumerGroup, MqMessageHolder messageHolder, boolean isOk) {

    }
}
