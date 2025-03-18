package org.noear.folkmq.broker.store.leveldb;

import com.github.artbits.quickio.core.IOEntity;

/**
 * @author noear 2025/1/2 created
 */
public class SubscribeDoc extends IOEntity {
    String topic;
    String queueName;
}
