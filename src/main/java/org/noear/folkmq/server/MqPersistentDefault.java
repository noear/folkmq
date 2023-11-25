package org.noear.folkmq.server;

import org.noear.socketd.transport.core.Message;
import org.noear.socketd.transport.core.Session;

/**
 * 消息持久化默认实现（空实现）
 *
 * @author noear
 * @since 1.0
 */
public class MqPersistentDefault implements MqPersistent{


    @Override
    public void init(MqServerInternal serverInternal) {

    }

    @Override
    public void onStartBefore() {

    }

    @Override
    public void onStartAfter() {

    }

    @Override
    public void onSubscribe(String topic, String consumer, Session session) {

    }

    @Override
    public void onPublish(String topic, Message message) {

    }

    @Override
    public void onDistribute(String consumer, MqMessageHolder messageHolder) {

    }

    @Override
    public void onAcknowledge(String consumer, MqMessageHolder messageHolder, boolean isOk) {

    }
}
