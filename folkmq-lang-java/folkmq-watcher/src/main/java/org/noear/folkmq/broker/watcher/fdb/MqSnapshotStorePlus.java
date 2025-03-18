package org.noear.folkmq.broker.watcher.fdb;


import org.noear.folkmq.broker.MqMessageHolder;
import org.noear.socketd.transport.core.Message;
import org.noear.socketd.transport.core.Session;
import org.noear.socketd.utils.RunUtils;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.LongAdder;

/**
 * 消息快照持久化（增加实现策略）
 * <br/>
 * 持久化定位为副本，只要重启时能恢复订阅关系与消息即可
 *
 * @author noear
 * @since 1.0
 */
public class MqSnapshotStorePlus extends MqSnapshotStore {
    private final LongAdder save900Count;
    private final LongAdder save300Count;
    private final LongAdder save100Count;

    private final ScheduledFuture<?> save900Future;
    private final ScheduledFuture<?> save300Future;
    private final ScheduledFuture<?> save100Future;

    protected long save900Condition = 1L;
    protected long save300Condition = 10L;
    protected long save100Condition = 10000L;

    public MqSnapshotStorePlus() {
        this(null);
    }

    public MqSnapshotStorePlus(String dataPath) {
        super(dataPath);

        this.save900Count = new LongAdder();
        this.save300Count = new LongAdder();
        this.save100Count = new LongAdder();

        int fixedDelay900 = 1000 * 900; //900秒
        this.save900Future = RunUtils.scheduleWithFixedDelay(this::onSave900, fixedDelay900, fixedDelay900);

        int fixedDelay300 = 1000 * 300; //300秒
        this.save300Future = RunUtils.scheduleWithFixedDelay(this::onSave300, fixedDelay300, fixedDelay300);

        int fixedDelay100 = 1000 * 100; //100秒
        this.save100Future = RunUtils.scheduleWithFixedDelay(this::onSave100, fixedDelay100, fixedDelay100);
    }

    public MqSnapshotStorePlus save900Condition(long save900Condition) {
        this.save900Condition = save900Condition;
        return this;
    }

    public MqSnapshotStorePlus save300Condition(long save300Condition) {
        this.save300Condition = save300Condition;

        return this;
    }

    public MqSnapshotStorePlus save100Condition(long save100Condition) {
        this.save100Condition = save100Condition;

        return this;
    }

    public long getSave900Count() {
        return save900Count.longValue();
    }

    public long getSave300Count() {
        return save300Count.longValue();
    }

    public long getSave100Count() {
        return save100Count.longValue();
    }

    private void onSave900() {
        long count = save900Count.sumThenReset();

        if (save900Condition > 1L) {
            if (count >= save900Condition) {
                onSave();
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("No trigger save900 condition!");
                }
            }
        }
    }

    private void onSave300() {
        long count = save300Count.sumThenReset();

        if (save300Condition > 0) {
            if (count >= save300Condition) {
                onSave();
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("No trigger save300 condition!");
                }
            }
        }
    }

    private void onSave100() {
        long count = save100Count.sumThenReset();

        if (save100Condition > 0) {
            if (count >= save100Condition) {
                onSave();
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("No trigger save100 condition!");
                }
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

        if (save100Future != null) {
            save100Future.cancel(false);
        }
    }

    @Override
    public void onSubscribe(String topic, String consumerGroup, Session session) {
        super.onSubscribe(topic, consumerGroup, session);
        onChange();
    }

    @Override
    public void onUnSubscribe(String topic, String consumerGroup, Session session) {
        super.onUnSubscribe(topic, consumerGroup, session);
        onChange();
    }

    @Override
    public void onPublish(Message message) {
        super.onPublish(message);
        onChange();
    }

    @Override
    public void onAcknowledge(String topic, String consumerGroup, MqMessageHolder messageHolder, boolean isOk) {
        super.onAcknowledge(topic, consumerGroup, messageHolder, isOk);
        onChange();
    }

    private void onChange() {
        //记数
        save900Count.increment();
        save300Count.increment();
        save100Count.increment();
    }
}
