package org.noear.folkmq.server;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

/**
 * @author noear
 * @since 1.5
 */
public class MqMessageHolderMap extends ConcurrentHashMap<String, MqMessageHolder> {
    private final LongAdder changedSize = new LongAdder();

    public LongAdder changedSize() {
        return changedSize;
    }

    @Override
    public MqMessageHolder put(String key, MqMessageHolder value) {
        changedSize.increment();
        return super.put(key, value);
    }

    @Override
    public MqMessageHolder remove(Object key) {
        changedSize.increment();
        return super.remove(key);
    }

    @Override
    public void clear() {
        changedSize.reset();
        super.clear();
    }
}
