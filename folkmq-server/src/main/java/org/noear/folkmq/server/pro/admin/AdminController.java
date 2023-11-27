package org.noear.folkmq.server.pro.admin;

import org.noear.folkmq.server.MqServerInternal;
import org.noear.folkmq.server.MqTopicConsumerQueue;
import org.noear.folkmq.server.pro.admin.model.QueueVo;
import org.noear.folkmq.server.pro.admin.model.TopicVo;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.core.handle.ModelAndView;
import org.noear.solon.core.handle.Result;
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
    MqServerInternal server;

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
        Map<String, MqTopicConsumerQueue> topicConsumerMap = server.getTopicConsumerMap();

        List<QueueVo> list = new ArrayList<>();

        for (String queue : new ArrayList<String>(topicConsumerMap.keySet())) {
            MqTopicConsumerQueue consumerQueue = topicConsumerMap.get(queue);

            QueueVo queueVo = new QueueVo();
            queueVo.setQueue(queue);

            if (consumerQueue != null) {
                queueVo.setMessageCount(consumerQueue.messageCount());
                queueVo.setSessionCount(consumerQueue.sessionCount());
            } else {
                queueVo.setMessageCount(0);
                queueVo.setSessionCount(0);
            }

            list.add(queueVo);
        }

        list.sort(Comparator.comparing(QueueVo::getQueue));

        return view("admin_queue").put("list", list);
    }

    @Mapping("/admin/save")
    public Result save() {
        server.save();

        return Result.succeed("Save successfully!");
    }
}