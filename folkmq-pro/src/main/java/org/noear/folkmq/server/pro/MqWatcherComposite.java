package org.noear.folkmq.server.pro;

import org.noear.folkmq.server.MqMessageHolder;
import org.noear.folkmq.server.MqWatcher;
import org.noear.folkmq.server.MqServiceInternal;
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
    private List<MqWatcher> persistentList = new ArrayList<>();

    /**
     * 添加持久化
     */
    public MqWatcherComposite add(MqWatcher persistent) {
        persistentList.add(persistent);
        return this;
    }

    /**
     * 移除持久化
     */
    public MqWatcherComposite remove(MqWatcher persistent) {
        persistentList.remove(persistent);
        return this;
    }

    @Override
    public void init(MqServiceInternal serverInternal) {
        for (MqWatcher persistent : persistentList) {
            persistent.init(serverInternal);
        }
    }

    @Override
    public void onStartBefore() {
        for (MqWatcher persistent : persistentList) {
            persistent.onStartBefore();
        }
    }

    @Override
    public void onStartAfter() {
        for (MqWatcher persistent : persistentList) {
            persistent.onStartAfter();
        }
    }

    @Override
    public void onStopBefore() {
        for (MqWatcher persistent : persistentList) {
            persistent.onStopBefore();
        }
    }

    @Override
    public void onStopAfter() {
        for (MqWatcher persistent : persistentList) {
            persistent.onStopAfter();
        }
    }

    @Override
    public void onSave() {
        for (MqWatcher persistent : persistentList) {
            persistent.onSave();
        }
    }

    @Override
    public void onSubscribe(String topic, String consumer, Session session) {
        for (MqWatcher persistent : persistentList) {
            persistent.onSubscribe(topic, consumer, session);
        }
    }

    @Override
    public void onUnSubscribe(String topic, String consumer, Session session) {
        for (MqWatcher persistent : persistentList) {
            persistent.onUnSubscribe(topic, consumer, session);
        }
    }

    @Override
    public void onPublish(Message message) {
        for (MqWatcher persistent : persistentList) {
            persistent.onPublish(message);
        }
    }

    @Override
    public void onDistribute(String topic, String consumer, MqMessageHolder messageHolder) {
        for (MqWatcher persistent : persistentList) {
            persistent.onDistribute(topic, consumer, messageHolder);
        }
    }

    @Override
    public void onAcknowledge(String topic, String consumer, MqMessageHolder messageHolder, boolean isOk) {
        for (MqWatcher persistent : persistentList) {
            persistent.onAcknowledge(topic, consumer, messageHolder, isOk);
        }
    }
}
