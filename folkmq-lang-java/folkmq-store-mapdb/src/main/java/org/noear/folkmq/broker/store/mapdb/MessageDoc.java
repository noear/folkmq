package org.noear.folkmq.broker.store.mapdb;

import java.io.Serializable;

/**
 * @author noear 2025/1/2 created
 */
public class MessageDoc implements Serializable {
    long id;
    String queueName;
    int ver;
    String metaString;
    String data;
}
