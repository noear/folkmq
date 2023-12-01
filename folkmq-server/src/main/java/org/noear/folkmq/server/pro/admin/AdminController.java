package org.noear.folkmq.server.pro.admin;

import org.noear.folkmq.common.MqConstants;
import org.noear.folkmq.server.MqServiceInternal;
import org.noear.folkmq.server.MqTopicConsumerQueue;
import org.noear.folkmq.server.MqTopicConsumerQueueDefault;
import org.noear.folkmq.server.pro.MqWatcherSnapshotPlus;
import org.noear.folkmq.server.pro.admin.model.QueueVo;
import org.noear.folkmq.server.pro.admin.model.TopicVo;
import org.noear.socketd.utils.RunUtils;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.core.handle.ModelAndView;
import org.noear.solon.validation.annotation.Logined;
import org.noear.solon.validation.annotation.Valid;

import java.util.*;

/**
 * 管理控制器
 *
 * @author noear
 * @since 1.0
 */
@Logined
@Valid
@Controller
public class AdminController extends BaseController {
    @Inject
    MqServiceInternal server;

    @Inject
    MqWatcherSnapshotPlus snapshotPlus;

    @Mapping("/admin")
    public ModelAndView admin() {
        return view("admin");
    }

    @Mapping("/admin/topic")
    public ModelAndView topic() {
        Map<String, Set<String>> subscribeMap = server.getSubscribeMap();

        List<TopicVo> list = new ArrayList<>();

        //用 list 转一下，免避线程安全
        for (String topic : new ArrayList<String>(subscribeMap.keySet())) {
            Set<String> queueSet = subscribeMap.get(topic);

            TopicVo topicVo = new TopicVo();
            topicVo.setTopic(topic);
            if (queueSet != null) {
                topicVo.setQueueCount(queueSet.size());
                topicVo.setQueueList(queueSet.toString());
            } else {
                topicVo.setQueueCount(0);
                topicVo.setQueueList("");
            }

            list.add(topicVo);

            //不超过99
            if (list.size() == 99) {
                break;
            }
        }

        list.sort(Comparator.comparing(TopicVo::getTopic));

        return view("admin_topic").put("list", list);
    }

    @Mapping("/admin/queue")
    public ModelAndView queue() {
        List<MqTopicConsumerQueue> mqTopicConsumerQueues = new ArrayList<>(server.getTopicConsumerMap().values());

        List<QueueVo> list = new ArrayList<>();

        for (MqTopicConsumerQueue tmp : mqTopicConsumerQueues) {
            MqTopicConsumerQueueDefault queue = (MqTopicConsumerQueueDefault) tmp;

            QueueVo queueVo = new QueueVo();
            queueVo.queue = queue.getTopic() + MqConstants.SEPARATOR_TOPIC_CONSUMER + queue.getConsumer();

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
        }

        list.sort(Comparator.comparing(QueueVo::getQueue));

        return view("admin_queue").put("list", list);
    }

    @Mapping("/admin/save")
    public String save() {
        if (snapshotPlus.inSaveProcess()) {
            return "save in process";
        } else {
            RunUtils.asyncAndTry(() -> {
                server.save();
            });

            return "A new save processing task begins";
        }
    }
}