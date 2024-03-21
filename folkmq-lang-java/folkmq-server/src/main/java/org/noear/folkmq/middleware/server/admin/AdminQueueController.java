package org.noear.folkmq.server.pro.admin;

import org.noear.folkmq.common.MqConstants;
import org.noear.folkmq.server.MqQueue;
import org.noear.folkmq.server.MqServiceInternal;
import org.noear.folkmq.server.pro.common.MetricsConfig;
import org.noear.folkmq.server.pro.MqWatcherSnapshotPlus;
import org.noear.folkmq.server.pro.admin.dso.QueueForceService;
import org.noear.folkmq.server.pro.admin.dso.ViewUtils;
import org.noear.folkmq.server.pro.admin.model.QueueVo;
import org.noear.socketd.transport.core.Session;
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
import java.util.List;

@Logined
@Valid
@Controller
public class AdminQueueController extends BaseController {
    static final Logger log = LoggerFactory.getLogger(AdminQueueController.class);

    @Inject
    private MqServiceInternal server;

    @Inject
    private MqWatcherSnapshotPlus snapshotPlus;

    @Inject
    private QueueForceService queueForceService;

    @Mapping("/admin/queue")
    public ModelAndView queue() {
        List<QueueVo> list = ViewUtils.queueView(server);

        return view("admin_queue").put("list", list);
    }

    @Mapping("/admin/queue_session")
    public ModelAndView queue_session(@NotEmpty String topic, @NotEmpty String consumerGroup) throws IOException {
        String queueName = topic + MqConstants.SEPARATOR_TOPIC_CONSUMER_GROUP + consumerGroup;
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

        return view("admin_queue_session").put("list", list);
    }


    @Mapping("/admin/queue_details")
    public ModelAndView queue_details(@NotEmpty String topic, @NotEmpty String consumerGroup) throws IOException {
        String queueName = topic + MqConstants.SEPARATOR_TOPIC_CONSUMER_GROUP + consumerGroup;

        return view("admin_queue_details")
                .put("topic", topic)
                .put("consumerGroup", consumerGroup);
    }

    @Post
    @Mapping("/admin/queue_details/ajax/distribute")
    public Result queue_details_ajax_distribute(@NotEmpty String topic, @NotEmpty String consumerGroup) {
        return queueForceService.forceDistribute(server, topic, consumerGroup, MetricsConfig.isStandalone);
    }

    @Post
    @Mapping("/admin/queue_details/ajax/delete")
    public Result queue_details_ajax_delete(@NotEmpty String topic, @NotEmpty String consumerGroup) {
        return queueForceService.forceDelete(server, topic, consumerGroup, MetricsConfig.isStandalone);
    }
}