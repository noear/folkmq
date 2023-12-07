package org.noear.folkmq.server.pro.admin.dso;

import org.noear.folkmq.server.MqServiceInternal;
import org.noear.folkmq.server.MqTopicConsumerQueue;
import org.noear.folkmq.server.MqTopicConsumerQueueDefault;
import org.noear.folkmq.server.pro.admin.model.QueueVo;

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
        List<MqTopicConsumerQueue> mqTopicConsumerQueues = new ArrayList<>(server.getTopicConsumerMap().values());

        //先排序，可以直接取前99 （旧方案是，全构建完成，再取99）
        mqTopicConsumerQueues.sort(Comparator.comparing(MqTopicConsumerQueue::getTopicConsumer));

        List<QueueVo> list = new ArrayList<>();

        for (MqTopicConsumerQueue tmp : mqTopicConsumerQueues) {
            MqTopicConsumerQueueDefault queue = (MqTopicConsumerQueueDefault) tmp;

            QueueVo queueVo = new QueueVo();
            queueVo.queue = queue.getTopicConsumer();

            //queueVo.isAlive = (queue.isAlive());
            queueVo.state = (queue.state().name());
            queueVo.sessionCount = (queue.sessionCount());
            queueVo.messageCount = (queue.messageTotal());

            queueVo.messageDelayedCount1 = queue.messageCounter(1);
            queueVo.messageDelayedCount2 = queue.messageCounter(2);
            queueVo.messageDelayedCount3 = queue.messageCounter(3);
            queueVo.messageDelayedCount4 = queue.messageCounter(4);
            queueVo.messageDelayedCount5 = queue.messageCounter(5);
            queueVo.messageDelayedCount6 = queue.messageCounter(6);
            queueVo.messageDelayedCount7 = queue.messageCounter(7);
            queueVo.messageDelayedCount8 = queue.messageCounter(8);


            list.add(queueVo);

            //不超过99
            if (list.size() == 99) {
                break;
            }
        }

        return list;
    }
}
