package org.noear.folkmq.broker.admin;

import org.noear.folkmq.broker.admin.dso.ViewQueueService;
import org.noear.folkmq.broker.admin.model.QueueVo;
import org.noear.folkmq.broker.mq.BrokerListenerFolkmq;
import org.noear.folkmq.common.MqConstants;
import org.noear.socketd.transport.core.Entity;
import org.noear.socketd.transport.core.Session;
import org.noear.socketd.transport.core.entity.StringEntity;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 队列管理控制器
 *
 * @author noear
 * @since 1.0
 */
@Logined
@Valid
@Controller
public class AdminQueueController extends BaseController{
    static final Logger log = LoggerFactory.getLogger(AdminQueueController.class);

    @Inject
    BrokerListenerFolkmq brokerListener;

    @Inject
    ViewQueueService viewQueueService;

    @Mapping("/admin/queue")
    public ModelAndView queue() {
        List<QueueVo> list = viewQueueService.getQueueListVo();
        list.sort(Comparator.comparing(v -> v.queue));

        return view("admin_queue").put("list", list);
    }

    @Mapping("/admin/queue_session")
    public ModelAndView queue_session(@NotEmpty String topic, @NotEmpty String consumerGroup) throws IOException {
        String queueName = topic + MqConstants.SEPARATOR_TOPIC_CONSUMER_GROUP + consumerGroup;
        List<String> list = new ArrayList<>();

        Collection<Session> sessionList = brokerListener.getPlayerAll(queueName);
        if (sessionList != null) {
            List<Session> sessions = new ArrayList<>(sessionList);
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


            QueueVo queueVo = viewQueueService.getQueueVo(queueName);
            if (queueVo != null) {
                if (queueVo.getSessionCount() > 0) {
                    return Result.failure("有消费者连接，不能删除!");
                }

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
}
