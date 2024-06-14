package org.noear.folkmq.broker;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 流速记录器
 *
 * @author noear
 * @since 1.5
 */
public class MqQps {
    private transient long startTime = System.currentTimeMillis();
    private transient AtomicLong counter = new AtomicLong();
    private long lastValue;
    private long maxValue;

    /**
     * 最后的值
     */
    public long getLastValue() {
        return lastValue;
    }

    /**
     * 最大的值
     */
    public long getMaxValue() {
        return maxValue;
    }

    /**
     * 记录
     */
    public void record() {
        counter.incrementAndGet();
    }

    /**
     * 重置
     */
    public void reset() {
        //保存
        long counterVal = counter.longValue();
        long elapsedTime = System.currentTimeMillis() - startTime;
        long elapsedSeconds = elapsedTime / 1000L;
        lastValue = counterVal / elapsedSeconds;
        if (lastValue > maxValue) {
            maxValue = lastValue;
        }

        //重新计数
        startTime = System.currentTimeMillis();
        counter.set(0L);
    }
}