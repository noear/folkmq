package org.noear.folkmq.broker.watcher;

import org.noear.folkmq.broker.MqMessageHolder;
import org.noear.folkmq.broker.MqWatcher;
import org.noear.folkmq.broker.MqBorkerInternal;
import org.noear.socketd.transport.core.Message;
import org.noear.socketd.transport.core.Session;

import java.util.ArrayList;
import java.util.List;

/**
 * 消息观察者 - 组合
 *
 * @author noear
 * @since 1.0
 */
public class MqWatcherComposite implements MqWatcher {
    private List<MqWatcher> watcherList = new ArrayList<>();

    /**
     * 添加持久化
     */
    public MqWatcherComposite add(MqWatcher persistent) {
        watcherList.add(persistent);
        return this;
    }

    /**
     * 移除持久化
     */
    public MqWatcherComposite remove(MqWatcher persistent) {
        watcherList.remove(persistent);
        return this;
    }

    @Override
    public void init(MqBorkerInternal serverInternal) {
        for (MqWatcher persistent : watcherList) {
            persistent.init(serverInternal);
        }
    }

    @Override
    public void onStartBefore() {
        for (MqWatcher persistent : watcherList) {
            persistent.onStartBefore();
        }
    }

    @Override
    public void onStartAfter() {
        for (MqWatcher persistent : watcherList) {
            persistent.onStartAfter();
        }
    }

    @Override
    public void onStopBefore() {
        for (MqWatcher persistent : watcherList) {
            persistent.onStopBefore();
        }
    }

    @Override
    public void onStopAfter() {
        for (MqWatcher persistent : watcherList) {
            persistent.onStopAfter();
        }
    }

    @Override
    public void onSave() {
        for (MqWatcher persistent : watcherList) {
            persistent.onSave();
        }
    }

    @Override
    public void onSubscribe(String topic, String consumerGroup, Session session) {
        for (MqWatcher persistent : watcherList) {
            persistent.onSubscribe(topic, consumerGroup, session);
        }
    }

    @Override
    public void onUnSubscribe(String topic, String consumerGroup, Session session) {
        for (MqWatcher persistent : watcherList) {
            persistent.onUnSubscribe(topic, consumerGroup, session);
        }
    }

    @Override
    public void onPublish(Message message) {
        for (MqWatcher persistent : watcherList) {
            persistent.onPublish(message);
        }
    }

    @Override
    public void onUnPublish(Message message) {
        for (MqWatcher persistent : watcherList) {
            persistent.onUnPublish(message);
        }
    }

    @Override
    public void onDistribute(String topic, String consumerGroup, MqMessageHolder messageHolder) {
        for (MqWatcher persistent : watcherList) {
            persistent.onDistribute(topic, consumerGroup, messageHolder);
        }
    }

    @Override
    public void onAcknowledge(String topic, String consumerGroup, MqMessageHolder messageHolder, boolean isOk) {
        for (MqWatcher persistent : watcherList) {
            persistent.onAcknowledge(topic, consumerGroup, messageHolder, isOk);
        }
    }
}
