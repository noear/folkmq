package org.noear.folkmq.broker.watcher.ldb;

import com.github.artbits.quickio.annotations.Index;
import com.github.artbits.quickio.core.IOEntity;

/**
 * @author noear 2025/1/2 created
 */
public class MessageDoc extends IOEntity {
    @Index
    String key; //Unique index
    String queueName;
    int ver;
    String metaString;
    String data;
}
