package org.noear.folkmq.middleware.broker.admin.dso;

import org.noear.folkmq.middleware.broker.admin.model.QueueVo;
import org.noear.folkmq.middleware.broker.mq.BrokerListenerFolkmq;
import org.noear.folkmq.common.MqConstants;
import org.noear.folkmq.common.MqMetasV1;
import org.noear.socketd.transport.core.Entity;
import org.noear.socketd.transport.core.Session;
import org.noear.socketd.transport.core.entity.StringEntity;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.core.handle.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author noear
 * @since 1.0
 */
@Component
public class QueueForceService {
    static final Logger log = LoggerFactory.getLogger(QueueForceService.class);
    static AtomicBoolean force_lock = new AtomicBoolean(false);

    @Inject
    BrokerListenerFolkmq brokerListener;

    @Inject
    ViewQueueService viewQueueService;

    public ViewQueueService getViewQueueService() {
        return viewQueueService;
    }

    /**
     * 强制派发
     */
    public Result forceDistribute(String topic, String consumerGroup) {
        if (force_lock.get()) {
            return Result.failure("正在进行别的强制操作!");
        }

        try {
            //增加安全锁控制
            force_lock.set(true);

            String queueName = topic + MqConstants.SEPARATOR_TOPIC_CONSUMER_GROUP + consumerGroup;

            log.warn("Queue forceDistribute: queueName={}", queueName);

            QueueVo queueVo = viewQueueService.getQueueVo(queueName);
            if (queueVo != null) {
                Collection<Session> tmp = brokerListener.getPlayerAll(MqConstants.BROKER_AT_SERVER);

                if (tmp == null || tmp.size() == 0) {
                    return Result.failure("没有消费者连接，不能派发!");
                }

                if (queueVo.getMessageCount() == 0) {
                    return Result.failure("没有消息可派发!");
                }

                List<Session> serverList = new ArrayList<>(tmp);
                Entity entity = new StringEntity("")
                        .metaPut(MqConstants.MQ_META_TOPIC, topic)
                        .metaPut(MqConstants.MQ_META_CONSUMER_GROUP, consumerGroup);

                for (Session s1 : serverList) {
                    s1.send(MqConstants.ADMIN_QUEUE_FORCE_DISTRIBUTE, entity);
                }

                return Result.succeed();
            } else {
                return Result.failure("没有找到队列!");
            }

        } catch (Throwable e) {
            return Result.failure(e.getLocalizedMessage());
        } finally {
            force_lock.set(false);
        }
    }

    /**
     * 强制删除
     */
    public Result forceDelete(String topic, String consumerGroup) {
        if (force_lock.get()) {
            return Result.failure("正在进行别的强制操作!");
        }

        try {
            //增加安全锁控制
            force_lock.set(true);
            String queueName = topic + MqConstants.SEPARATOR_TOPIC_CONSUMER_GROUP + consumerGroup;

            log.warn("Queue forceDelete: queueName={}", queueName);

            QueueVo queueVo = viewQueueService.getQueueVo(queueName);
            if (queueVo != null) {
                if (queueVo.getSessionCount() > 0) {
                    return Result.failure("有消费者连接，不能删除!");
                }

                viewQueueService.removeQueueVo(queueName);
                brokerListener.removeSubscribe(topic, queueName);

                Collection<Session> tmp = brokerListener.getPlayerAll(MqConstants.BROKER_AT_SERVER);
                List<Session> serverList = new ArrayList<>(tmp);
                Entity entity = new StringEntity("")
                        .metaPut(MqConstants.MQ_META_TOPIC, topic)
                        .metaPut(MqConstants.MQ_META_CONSUMER_GROUP, consumerGroup);

                for (Session s1 : serverList) {
                    s1.send(MqConstants.ADMIN_QUEUE_FORCE_DELETE, entity);
                }

                return Result.succeed();
            } else {
                return Result.failure("没有找到队列!");
            }

        } catch (Throwable e) {
            return Result.failure(e.getLocalizedMessage());
        } finally {
            force_lock.set(false);
        }
    }

    /**
     * 强制清空
     */
    public Result forceClear( String topic, String consumerGroup) {
        if (force_lock.get()) {
            return Result.failure("正在进行别的强制操作!");
        }

        try {
            //增加安全锁控制
            force_lock.set(true);
            String queueName = topic + MqConstants.SEPARATOR_TOPIC_CONSUMER_GROUP + consumerGroup;

            log.warn("Queue forceClear: queueName={}", queueName);

            QueueVo queueVo = viewQueueService.getQueueVo(queueName);
            if (queueVo != null) {
                if (queueVo.getSessionCount() > 0) {
                    return Result.failure("有消费者连接，不能删除!");
                }

                viewQueueService.removeQueueVo(queueName);
                brokerListener.removeSubscribe(topic, queueName);

                Collection<Session> tmp = brokerListener.getPlayerAll(MqConstants.BROKER_AT_SERVER);
                List<Session> serverList = new ArrayList<>(tmp);
                Entity entity = new StringEntity("")
                        .metaPut(MqConstants.MQ_META_TOPIC, topic)
                        .metaPut(MqConstants.MQ_META_CONSUMER_GROUP, consumerGroup);

                for (Session s1 : serverList) {
                    s1.send(MqConstants.ADMIN_QUEUE_FORCE_CLEAR, entity);
                }

                return Result.succeed();
            } else {
                return Result.failure("没有找到队列!");
            }

        } catch (Throwable e) {
            return Result.failure(e.getLocalizedMessage());
        } finally {
            force_lock.set(false);
        }
    }
}
