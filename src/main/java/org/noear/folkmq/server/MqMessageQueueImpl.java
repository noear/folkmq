package org.noear.folkmq.server;

import org.noear.folkmq.MqConstants;
import org.noear.socketd.transport.core.Session;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author noear
 * @since 1.0
 */
public class MqMessageQueueImpl implements MqMessageQueue {
    private Queue<MqMessageHolder> queue = new LinkedList<>();
    private Queue<MqMessageHolder> delayedQueue = new LinkedList<>();

    private final String identity;
    private final Set<Session> sessionSet;

    public MqMessageQueueImpl(String identity, Set<Session> sessionSet) {
        this.identity = identity;
        this.sessionSet = sessionSet;
    }

    @Override
    public String getIdentity() {
        return identity;
    }

    @Override
    public void add(MqMessageHolder messageHolder) {
        queue.add(messageHolder);

        distribute();
    }

    /**
     * 派发
     */
    private void distribute() {
        //找到此身份的其中一个会话（如果是 ip 就一个；如果是集群名则任选一个）
        List<Session> sessions = sessionSet.parallelStream()
                .filter(s -> s.attrMap().containsKey(identity))
                .collect(Collectors.toList());

        if (sessions.size() > 0) {

            MqMessageHolder messageHolder;
            while (true) {
                messageHolder = queue.poll();
                if (messageHolder == null) {
                    break;
                }

                if (MqNextTime.allowDistribute(messageHolder) == false) {
                    //进入延后队列
                    delayedQueue.add(messageHolder);
                    continue;
                }

                try {
                    distributeDo(messageHolder, sessions);
                } catch (Exception e) {
                    //进入延后队列
                    delayedQueue.add(messageHolder.deferred());
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

        //todo:这里可能会有线程同步问题
        messageHolder.getMessage().data().reset();

        //给会话发送消息
        s1.sendAndSubscribe(MqConstants.MQ_CMD_DISTRIBUTE, messageHolder.getMessage(), m -> {
            int ack = Integer.parseInt(m.metaOrDefault(MqConstants.MQ_ACK, "0"));
            if (ack == 0) {
                //进入延后队列，之后再试 //todo:如果因为网络原因，没有回调怎么办？
                delayedQueue.add(messageHolder.deferred());
            }
        });
    }
}
