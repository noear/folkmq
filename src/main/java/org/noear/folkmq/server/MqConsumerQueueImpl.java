package org.noear.folkmq.server;

import org.noear.folkmq.MqConstants;
import org.noear.socketd.transport.core.Session;
import org.noear.socketd.utils.RunUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 * 消费者队列（一个消费者一个队列，一个消费者可多个会话）
 *
 * @author noear
 * @since 1.0
 */
public class MqConsumerQueueImpl implements MqConsumerQueue {
    private static final Logger log = LoggerFactory.getLogger(MqConsumerQueueImpl.class);

    //用户
    private final String consumer;
    //用户会话
    private final List<Session> userSessionSet;

    public MqConsumerQueueImpl(String consumer) {
        this.consumer = consumer;
        this.userSessionSet = new ArrayList<>();
    }

    @Override
    public void addSession(Session session) {
        userSessionSet.add(session);
    }

    @Override
    public void removeSession(Session session) {
        userSessionSet.remove(session);
    }

    @Override
    public synchronized void push(MqMessageHolder messageHolder) {
        distribute(messageHolder);
    }

    /**
     * 添加延时处理
     */
    private void addDelayed(MqMessageHolder messageHolder) {
        addDelayed(messageHolder, messageHolder.getNextTime() - System.currentTimeMillis());
    }

    /**
     * 添加延时处理
     *
     * @param millisDelay 延时（单位：毫秒）
     */
    private void addDelayed(MqMessageHolder messageHolder, long millisDelay) {
        synchronized (messageHolder) {
            if (messageHolder.deferredFuture != null) {
                messageHolder.deferredFuture.cancel(true);
            }

            messageHolder.deferredFuture = RunUtils.delay(() -> {
                push(messageHolder);
            }, millisDelay);
        }
    }

    /**
     * 清理延时处理
     */
    public void clearDelayed(MqMessageHolder messageHolder) {
        synchronized (messageHolder) {
            if (messageHolder.deferredFuture != null) {
                messageHolder.deferredFuture.cancel(true);
                //messageHolder.deferredFuture = null;
            }
        }
    }


    /**
     * 执行派发
     */
    private void distribute(MqMessageHolder messageHolder) {
        //找到此身份的其中一个会话（如果是 ip 就一个；如果是集群名则任选一个）
        if (userSessionSet.size() > 0) {
            if (MqNextTime.allowDistribute(messageHolder) == false) {
                //进入延后队列
                addDelayed(messageHolder);
            } else {
                try {
                    distributeDo(messageHolder, userSessionSet);
                } catch (Throwable e) {
                    //进入延后队列
                    addDelayed(messageHolder.deferred());
                }
            }
        } else {
            //进入延后队列
            addDelayed(messageHolder.deferred());
            log.warn("No sessions!");
        }
    }

    /**
     * 派发执行
     */
    private void distributeDo(MqMessageHolder messageHolder, List<Session> sessions) throws IOException {
        //随机取一个会话
        int idx = 0;
        if (sessions.size() > 1) {
            idx = new Random().nextInt(sessions.size());
        }
        Session s1 = sessions.get(idx);

        messageHolder.getContent()
                .meta(MqConstants.MQ_TIMES, String.valueOf(messageHolder.getTimes()));


        //添加延时任务：30秒后，如果没有没有答复就重发
        addDelayed(messageHolder, 30 * 10);

        //给会话发送消息
        s1.sendAndSubscribe(MqConstants.MQ_CMD_DISTRIBUTE, messageHolder.getContent(), m -> {
            int ack = Integer.parseInt(m.metaOrDefault(MqConstants.MQ_ACK, "0"));
            if (ack == 0) {
                //no, 进入延后队列，之后再试
                addDelayed(messageHolder.deferred());
            } else {
                //ok
                clearDelayed(messageHolder);
            }
        });
    }
}