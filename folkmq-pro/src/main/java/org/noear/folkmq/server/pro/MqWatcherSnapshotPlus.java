package org.noear.folkmq.server.pro;


import org.noear.folkmq.server.MqMessageHolder;
import org.noear.socketd.transport.core.Message;
import org.noear.socketd.transport.core.Session;
import org.noear.socketd.utils.RunUtils;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 消息观察者 - 快照持久化（增加实现策略）
 * <br/>
 * 持久化定位为副本，只要重启时能恢复订阅关系与消息即可
 *
 * @author noear
 * @since 1.0
 */
public class MqWatcherSnapshotPlus extends MqWatcherSnapshot{
    private final AtomicLong save900Count;
    private final AtomicLong save300Count;
    private final AtomicLong save60Count;

    private final ScheduledFuture<?> save900Future;
    private final ScheduledFuture<?> save300Future;
    private final ScheduledFuture<?> save60Future;

    protected long save900Condition = 1L;
    protected long save300Condition = 10L;
    protected long save60Condition = 10000L;

    public MqWatcherSnapshotPlus() {
        this(null);
    }

    public MqWatcherSnapshotPlus(String dataPath) {
        super(dataPath);

        this.save900Count = new AtomicLong();
        this.save300Count = new AtomicLong();
        this.save60Count = new AtomicLong();

        int fixedDelay900 = 1000 * 900; //900秒
        this.save900Future = RunUtils.scheduleWithFixedDelay(this::onSave900, fixedDelay900, fixedDelay900);

        int fixedDelay300 = 1000 * 300; //300秒
        this.save300Future = RunUtils.scheduleWithFixedDelay(this::onSave300, fixedDelay300, fixedDelay300);

        int fixedDelay60 = 1000 * 60; //60秒
        this.save60Future = RunUtils.scheduleWithFixedDelay(this::onSave60, fixedDelay60, fixedDelay60);
    }

    public MqWatcherSnapshotPlus save900Condition(long save900Condition) {
        if (save900Condition >= 1L) {
            this.save900Condition = save900Condition;
        }
        return this;
    }

    public MqWatcherSnapshotPlus save300Condition(long save300Condition) {
        if (save300Condition >= 1L) {
            this.save300Condition = save300Condition;
        }

        return this;
    }

    public MqWatcherSnapshotPlus save60Condition(long save60Condition) {
        if (save60Condition >= 1L) {
            this.save60Condition = save60Condition;
        }

        return this;
    }

    public long getSave900Count() {
        return save900Count.get();
    }

    public long getSave300Count() {
        return save300Count.get();
    }

    public long getSave60Count() {
        return save60Count.get();
    }

    private void onSave900() {
        long count = save900Count.get();
        save900Count.set(0);

        if (count >= save900Condition) {
            onSave();
        } else {
            if (log.isDebugEnabled()) {
                log.debug("No trigger save900 condition!");
            }
        }
    }

    private void onSave300() {
        long count = save300Count.get();
        save300Count.set(0);

        if (count >= save300Condition) {
            onSave();
        } else {
            if (log.isDebugEnabled()) {
                log.debug("No trigger save300 condition!");
            }
        }
    }

    private void onSave60() {
        long count = save60Count.get();
        save60Count.set(0);

        if (count >= save60Condition) {
            onSave();
        } else {
            if (log.isDebugEnabled()) {
                log.debug("No trigger save60 condition!");
            }
        }
    }

    @Override
    public void onStopBefore() {
        if (save900Future != null) {
            save900Future.cancel(false);
        }

        if (save300Future != null) {
            save300Future.cancel(false);
        }

        if (save60Future != null) {
            save60Future.cancel(false);
        }
    }

    @Override
    public void onSubscribe(String topic, String consumer, Session session) {
        super.onSubscribe(topic, consumer, session);
        onChange();
    }

    @Override
    public void onUnSubscribe(String topic, String consumer, Session session) {
        super.onUnSubscribe(topic, consumer, session);
        onChange();
    }

    @Override
    public void onPublish(Message message) {
        super.onPublish(message);
        onChange();
    }

    @Override
    public void onAcknowledge(String consumer, MqMessageHolder messageHolder, boolean isOk) {
        super.onAcknowledge(consumer, messageHolder, isOk);
        onChange();
    }

    private void onChange() {
        //记数
        save900Count.incrementAndGet();
        save300Count.incrementAndGet();
        save60Count.incrementAndGet();
    }
}
