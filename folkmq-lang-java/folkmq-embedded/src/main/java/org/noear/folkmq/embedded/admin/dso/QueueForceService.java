package org.noear.folkmq.embedded.admin.dso;

import org.noear.folkmq.common.MqConstants;
import org.noear.folkmq.server.MqQueue;
import org.noear.folkmq.server.MqServiceInternal;
import org.noear.solon.annotation.Component;
import org.noear.solon.core.handle.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 队列强制操作服务
 *
 * @author noear
 * @since 1.0
 */
@Component
public class QueueForceService {
    static final Logger log = LoggerFactory.getLogger(QueueForceService.class);
    static AtomicBoolean force_lock = new AtomicBoolean(false);

    /**
     * 强制派发
     */
    public Result forceDistribute(MqServiceInternal server, String topic, String consumerGroup, boolean isStandalone) {
        if (force_lock.get()) {
            return Result.failure("正在进行别的强制操作!");
        }

        try {
            //增加安全锁控制
            force_lock.set(true);

            String queueName = topic + MqConstants.SEPARATOR_TOPIC_CONSUMER_GROUP + consumerGroup;

            log.warn("Queue forceDistribute: queueName={}", queueName);

            MqQueue queue = server.getQueue(queueName);
            if (queue != null) {
                if (queue.sessionCount() == 0 && isStandalone) {
                    return Result.failure("没有消费者连接，不能派发!");
                }

                if (queue.messageTotal() == 0) {
                    return Result.failure("没有消息可派发!");
                }

                queue.forceDistribute(1, 0);

                return Result.succeed();
            } else {
                return Result.failure("没有找到队列!");
            }
        } finally {
            force_lock.set(false);
        }
    }

    /**
     * 强制删除
     */
    public Result forceDelete(MqServiceInternal server, String topic, String consumerGroup, boolean isStandalone) {
        if (force_lock.get()) {
            return Result.failure("正在进行别的强制操作!");
        }

        try {
            //增加安全锁控制
            force_lock.set(true);
            String queueName = topic + MqConstants.SEPARATOR_TOPIC_CONSUMER_GROUP + consumerGroup;

            log.warn("Queue forceDelete: queueName={}", queueName);

            MqQueue queue = server.getQueue(queueName);
            if (queue != null) {
                if (queue.sessionCount() > 0 && isStandalone) {
                    return Result.failure("有消费者连接，不能删除!");
                }

                server.removeQueue(queueName);
                queue.forceClear();

                return Result.succeed();
            } else {
                return Result.failure("没有找到队列!");
            }
        } finally {
            force_lock.set(false);
        }
    }

    /**
     * 强制清空
     */
    public Result forceClear(MqServiceInternal server, String topic, String consumerGroup, boolean isStandalone) {
        if (force_lock.get()) {
            return Result.failure("正在进行别的强制操作!");
        }

        try {
            //增加安全锁控制
            force_lock.set(true);
            String queueName = topic + MqConstants.SEPARATOR_TOPIC_CONSUMER_GROUP + consumerGroup;

            log.warn("Queue forceClear: queueName={}", queueName);

            MqQueue queue = server.getQueue(queueName);
            if (queue != null) {
                if (queue.sessionCount() > 0 && isStandalone) {
                    return Result.failure("有消费者连接，不能清空!");
                }

                queue.forceClear();

                return Result.succeed();
            } else {
                return Result.failure("没有找到队列!");
            }
        } finally {
            force_lock.set(false);
        }
    }
}