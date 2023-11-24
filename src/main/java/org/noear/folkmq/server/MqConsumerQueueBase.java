package org.noear.folkmq.server;

import org.noear.socketd.utils.RunUtils;

/**
 * 消费者队列基类（把延时处理分到此类）
 *
 * @author noear
 * @since 1.0
 */
public abstract class MqConsumerQueueBase implements MqConsumerQueue {
    /**
     * 添加延时处理
     */
    protected void addDelayed(MqMessageHolder messageHolder) {
        addDelayed(messageHolder, messageHolder.getDistributeTime() - System.currentTimeMillis());
    }

    /**
     * 添加延时处理
     *
     * @param millisDelay 延时（单位：毫秒）
     */
    protected void addDelayed(MqMessageHolder messageHolder, long millisDelay) {
        synchronized (messageHolder) {
            if (messageHolder.delayedFuture != null) {
                messageHolder.delayedFuture.cancel(true);
            }

            messageHolder.delayedFuture = RunUtils.delay(() -> {
                push(messageHolder);
            }, millisDelay);
        }
    }

    /**
     * 清理延时处理
     */
    protected void clearDelayed(MqMessageHolder messageHolder) {
        synchronized (messageHolder) {
            if (messageHolder.delayedFuture != null) {
                messageHolder.delayedFuture.cancel(true);
                messageHolder.delayedFuture = null;
            }
        }
    }
}
