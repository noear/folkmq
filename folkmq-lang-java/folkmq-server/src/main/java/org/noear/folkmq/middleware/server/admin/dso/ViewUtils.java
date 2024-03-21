package org.noear.folkmq.middleware.server.admin.dso;

import org.noear.folkmq.server.MqServiceInternal;
import org.noear.folkmq.server.MqQueue;
import org.noear.folkmq.server.MqQueueDefault;
import org.noear.folkmq.middleware.server.admin.model.QueueVo;
import org.noear.socketd.transport.core.Session;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 数据视图工具
 *
 * @author noear
 * @since 1.0
 */
public class ViewUtils {
    public static List<QueueVo> queueView(MqServiceInternal server) {
        List<MqQueue> queueList = new ArrayList<>(server.getQueueMap().values());

        //先排序，可以直接取前99 （旧方案是，全构建完成，再取99）
        queueList.sort(Comparator.comparing(MqQueue::getQueueName));

        List<QueueVo> list = new ArrayList<>();

        for (MqQueue tmp : queueList) {
            MqQueueDefault queue = (MqQueueDefault) tmp;

            QueueVo queueVo = new QueueVo();
            queueVo.queue = queue.getQueueName();

            queueVo.sessionCount = (queue.sessionCount());
            queueVo.messageCount = (queue.messageTotal());

            queueVo.messageDelayedCount1 = queue.messageCount(1);
            queueVo.messageDelayedCount2 = queue.messageCount(2);
            queueVo.messageDelayedCount3 = queue.messageCount(3);
            queueVo.messageDelayedCount4 = queue.messageCount(4);
            queueVo.messageDelayedCount5 = queue.messageCount(5);
            queueVo.messageDelayedCount6 = queue.messageCount(6);
            queueVo.messageDelayedCount7 = queue.messageCount(7);
            queueVo.messageDelayedCount8 = queue.messageCount(8);


            list.add(queueVo);

            //不超过99
            if (list.size() == 99) {
                break;
            }
        }

        return list;
    }

    public static QueueVo queueOneView(MqServiceInternal server, String queueName) {
        MqQueueDefault queue = (MqQueueDefault) server.getQueue(queueName);
        if(queue == null){
            return null;
        }

        QueueVo queueVo = new QueueVo();
        queueVo.queue = queue.getQueueName();

        queueVo.sessionCount = (queue.sessionCount());
        queueVo.messageCount = (queue.messageTotal());

        queueVo.messageDelayedCount1 = queue.messageCount(1);
        queueVo.messageDelayedCount2 = queue.messageCount(2);
        queueVo.messageDelayedCount3 = queue.messageCount(3);
        queueVo.messageDelayedCount4 = queue.messageCount(4);
        queueVo.messageDelayedCount5 = queue.messageCount(5);
        queueVo.messageDelayedCount6 = queue.messageCount(6);
        queueVo.messageDelayedCount7 = queue.messageCount(7);
        queueVo.messageDelayedCount8 = queue.messageCount(8);

        return queueVo;
    }

    public static List<String> queueSessionListView(MqServiceInternal server, String queueName) throws IOException {
        List<String> list = new ArrayList<>();

        MqQueue queue = server.getQueue(queueName);
        if (queue != null) {
            List<Session> sessions = new ArrayList<>(queue.getSessions());
            for (Session s1 : sessions) {
                list.add(s1.remoteAddress().toString());

                //不超过99
                if (list.size() == 99) {
                    break;
                }
            }
        }

        return list;
    }
}
