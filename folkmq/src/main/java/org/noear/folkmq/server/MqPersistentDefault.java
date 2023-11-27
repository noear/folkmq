package org.noear.folkmq.server;

import org.noear.socketd.transport.core.Message;
import org.noear.socketd.transport.core.Session;

/**
 * 消息持久化默认实现（持久化定位为副本，只要重启时能恢复订阅关系与消息即可）
 * <br/>
 * 关键：onStart.., onStop.., onSubscribe, onPublish
 * 提示：onSubscribe, onPublish 做同步处理（可靠性高），做异步处理（性能高）。具体看场景需求
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
    public void onStopBefore() {

    }

    @Override
    public void onStopAfter() {

    }

    @Override
    public void onSave() {

    }

    @Override
    public void onSubscribe(String topic, String consumer, Session session) {

    }

    @Override
    public void onUnSubscribe(String topic, String consumer, Session session) {

    }

    @Override
    public void onPublish(Message message) {

    }

    @Override
    public void onDistribute(String consumer, MqMessageHolder messageHolder) {

    }

    @Override
    public void onAcknowledge(String consumer, MqMessageHolder messageHolder, boolean isOk) {

    }
}
