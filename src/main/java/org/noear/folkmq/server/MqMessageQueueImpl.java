package org.noear.folkmq.server;

import org.noear.folkmq.MqConstants;
import org.noear.socketd.transport.core.Session;
import org.noear.socketd.utils.RunUtils;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * @author noear
 * @since 1.0
 */
public class MqMessageQueueImpl implements MqMessageQueue {
    private Queue<MqMessageHolder> queue = new LinkedList<>();

    private final String identity;
    private final List<Session> subscriberSet;
    private CompletableFuture<?> distributeFuture;

    public MqMessageQueueImpl(String identity) {
        this.identity = identity;
        this.subscriberSet  = new ArrayList<>();
    }

    @Override
    public void addSubscriber(Session session){
        subscriberSet.add(session);
    }

    @Override
    public void removeSubscriber(Session session) {
        subscriberSet.remove(session);
    }

    @Override
    public String getIdentity() {
        return identity;
    }

    @Override
    public void push(MqMessageHolder messageHolder) {
        queue.add(messageHolder);

        if (distributeFuture == null) {
            RunUtils.async(() -> {
                distribute();
                distributeFuture = null;
            });
        }
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
        if (messageHolder.deferredFuture != null) {
            messageHolder.deferredFuture.cancel(true);
        }

        messageHolder.deferredFuture = RunUtils.delay(() -> {
            push(messageHolder);
        }, millisDelay);
    }

    /**
     * 清处延时
     */
    public void clearDelayed(MqMessageHolder messageHolder) {
        if (messageHolder.deferredFuture != null) {
            messageHolder.deferredFuture.cancel(true);
            messageHolder.deferredFuture = null;
        }
    }


    /**
     * 派发
     */
    private void distribute() {
        //找到此身份的其中一个会话（如果是 ip 就一个；如果是集群名则任选一个）
        if (subscriberSet.size() > 0) {

            MqMessageHolder messageHolder;
            while (true) {
                messageHolder = queue.poll();
                if (messageHolder == null) {
                    break;
                }

                if (MqNextTime.allowDistribute(messageHolder) == false) {
                    //进入延后队列
                    addDelayed(messageHolder);
                    continue;
                }

                try {
                    distributeDo(messageHolder, subscriberSet);
                } catch (Throwable e) {
                    //进入延后队列
                    addDelayed(messageHolder.deferred());
                }
            }
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

        messageHolder.getContent().meta(MqConstants.MQ_TIMES, String.valueOf(messageHolder.getTimes()));


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
