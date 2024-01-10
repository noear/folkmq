package org.noear.folkmq.server.pro.admin;

import org.noear.folkmq.client.MqMessage;
import org.noear.folkmq.common.MqConstants;
import org.noear.folkmq.common.MqUtils;
import org.noear.folkmq.server.MqQueue;
import org.noear.folkmq.server.MqServiceInternal;
import org.noear.folkmq.server.pro.MqWatcherSnapshotPlus;
import org.noear.folkmq.server.pro.admin.dso.ViewUtils;
import org.noear.folkmq.server.pro.admin.model.QueueVo;
import org.noear.folkmq.server.pro.admin.model.TopicVo;
import org.noear.snack.core.utils.DateUtil;
import org.noear.socketd.transport.core.Message;
import org.noear.socketd.transport.core.Session;
import org.noear.socketd.utils.RunUtils;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.annotation.Post;
import org.noear.solon.core.handle.ModelAndView;
import org.noear.solon.core.handle.Result;
import org.noear.solon.validation.annotation.Logined;
import org.noear.solon.validation.annotation.NotEmpty;
import org.noear.solon.validation.annotation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

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
    static final Logger log = LoggerFactory.getLogger(AdminController.class);

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
        Iterator<Map.Entry<String, Set<String>>> iterator = server.getSubscribeMap().entrySet().iterator();

        List<TopicVo> list = new ArrayList<>();

        //用 list 转一下，免避线程安全
        while (iterator.hasNext()) {
            Map.Entry<String, Set<String>> kv = iterator.next();
            String topic = kv.getKey();
            Set<String> queueSet = kv.getValue();

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
        List<QueueVo> list = ViewUtils.queueView(server);

        return view("admin_queue").put("list", list);
    }

    @Mapping("/admin/queue_session")
    public ModelAndView queue_session(@NotEmpty String topic, @NotEmpty String consumerGroup) throws IOException {
        String queueName = topic + MqConstants.SEPARATOR_TOPIC_CONSUMER_GROUP + consumerGroup;
        List<String> list = new ArrayList<>();

        MqQueue queue = server.getQueueMap().get(queueName);
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

        return view("admin_queue_session").put("list", list);
    }


    @Mapping("/admin/queue_details")
    public ModelAndView queue_details(@NotEmpty String topic, @NotEmpty String consumerGroup) throws IOException {
        String queueName = topic + MqConstants.SEPARATOR_TOPIC_CONSUMER_GROUP + consumerGroup;


        return view("admin_queue_details")
                .put("topic", topic)
                .put("consumerGroup", consumerGroup);
    }


    static AtomicBoolean force_lock = new AtomicBoolean(false);

    @Post
    @Mapping("/admin/queue_details/ajax/distribute")
    public Result queue_details_ajax_distribute(@NotEmpty String topic, @NotEmpty String consumerGroup) {
        if (force_lock.get()) {
            return Result.failure("正在进行别的强制操作!");
        }

        try {
            //增加安全锁控制
            force_lock.set(true);

            String queueName = topic + MqConstants.SEPARATOR_TOPIC_CONSUMER_GROUP + consumerGroup;

            log.warn("Queue forceDistribute: queueName={}", queueName);

            MqQueue queue = server.getQueueMap().get(queueName);
            if (queue != null) {
                if (queue.sessionCount() == 0) {
                    return Result.failure("没有消费者连接，不能派发!");
                }

                if (queue.messageTotal() == 0) {
                    return Result.failure("没有消息可派发!");
                }

                queue.forceDistribute(2, 0);

                return Result.succeed();
            } else {
                return Result.failure("没有找到队列!");
            }
        } finally {
            force_lock.set(false);
        }
    }

    @Post
    @Mapping("/admin/queue_details/ajax/delete")
    public Result queue_details_ajax_delete(@NotEmpty String topic, @NotEmpty String consumerGroup) {
        if (force_lock.get()) {
            return Result.failure("正在进行别的强制操作!");
        }

        try {
            //增加安全锁控制
            force_lock.set(true);
            String queueName = topic + MqConstants.SEPARATOR_TOPIC_CONSUMER_GROUP + consumerGroup;

            log.warn("Queue forceDelete: queueName={}", queueName);

            MqQueue queue = server.getQueueMap().get(queueName);
            if (queue != null) {
                if (queue.sessionCount() > 0) {
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

    @Mapping("/admin/publish")
    public ModelAndView publish() {
        return view("admin_publish");
    }

    @Mapping("/admin/publish/ajax/post")
    public Result publish_ajax_post(String topic, String scheduled, int qos, String content) {
        try {
            Date scheduledDate = DateUtil.parse(scheduled);

            MqMessage message = new MqMessage(content).qos(qos).scheduled(scheduledDate);
            Message routingMessage = MqUtils.routingMessageBuild(topic, message);

            server.routingDo(routingMessage);

            return Result.succeed();
        } catch (Exception e) {
            return Result.failure(e.getLocalizedMessage());
        }
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