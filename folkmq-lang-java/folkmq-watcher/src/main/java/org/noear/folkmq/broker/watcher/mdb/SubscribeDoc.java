package org.noear.folkmq.broker.watcher.mdb;

import java.io.Serializable;

/**
 * @author noear 2025/1/2 created
 */
public class SubscribeDoc implements Serializable {
    String topic;
    String queueName;
}
