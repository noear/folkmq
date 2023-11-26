package org.noear.folkmq.server.pro;

import org.noear.folkmq.server.MqMessageHolder;
import org.noear.folkmq.server.MqPersistent;
import org.noear.folkmq.server.MqServerInternal;
import org.noear.socketd.transport.core.Message;
import org.noear.socketd.transport.core.Session;

import java.util.ArrayList;
import java.util.List;

/**
 * 持久化 - 组合
 *
 * @author noear
 * @since 1.0
 */
public class MqPersistentComposite implements MqPersistent {
    private List<MqPersistent> persistentList = new ArrayList<>();

    /**
     * 添加持久化
     */
    public void addPersistent(MqPersistent persistent) {
        persistentList.add(persistent);
    }

    /**
     * 移除持久化
     */
    public void removePersistent(MqPersistent persistent) {
        persistentList.remove(persistent);
    }

    @Override
    public void init(MqServerInternal serverInternal) {
        for (MqPersistent persistent : persistentList) {
            persistent.init(serverInternal);
        }
    }

    @Override
    public void onStartBefore() {
        for (MqPersistent persistent : persistentList) {
            persistent.onStartBefore();
        }
    }

    @Override
    public void onStartAfter() {
        for (MqPersistent persistent : persistentList) {
            persistent.onStartAfter();
        }
    }

    @Override
    public void onStopBefore() {
        for (MqPersistent persistent : persistentList) {
            persistent.onStopBefore();
        }
    }

    @Override
    public void onStopAfter() {
        for (MqPersistent persistent : persistentList) {
            persistent.onStopAfter();
        }
    }

    @Override
    public void onSave() {
        for (MqPersistent persistent : persistentList) {
            persistent.onSave();
        }
    }

    @Override
    public void onSubscribe(String topic, String consumer, Session session) {
        for (MqPersistent persistent : persistentList) {
            persistent.onSubscribe(topic, consumer, session);
        }
    }

    @Override
    public void onUnSubscribe(String topic, String consumer, Session session) {
        for (MqPersistent persistent : persistentList) {
            persistent.onUnSubscribe(topic, consumer, session);
        }
    }

    @Override
    public void onPublish(String topic, Message message) {
        for (MqPersistent persistent : persistentList) {
            persistent.onPublish(topic, message);
        }
    }

    @Override
    public void onDistribute(String consumer, MqMessageHolder messageHolder) {
        for (MqPersistent persistent : persistentList) {
            persistent.onDistribute(consumer, messageHolder);
        }
    }

    @Override
    public void onAcknowledge(String consumer, MqMessageHolder messageHolder, boolean isOk) {
        for (MqPersistent persistent : persistentList) {
            persistent.onAcknowledge(consumer, messageHolder, isOk);
        }
    }
}
