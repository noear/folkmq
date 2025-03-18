package org.noear.folkmq.broker.store.mapdb;

import java.io.Serializable;

/**
 * @author noear 2025/1/2 created
 */
public class SubscribeDoc implements Serializable {
    String topic;
    String queueName;
}
