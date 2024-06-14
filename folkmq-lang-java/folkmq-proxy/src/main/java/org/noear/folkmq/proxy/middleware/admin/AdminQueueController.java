package org.noear.folkmq.proxy.middleware.admin;

import org.noear.folkmq.proxy.middleware.admin.dso.QueueForceService;
import org.noear.folkmq.proxy.middleware.admin.dso.ViewQueueService;
import org.noear.folkmq.proxy.middleware.admin.model.QueueVo;
import org.noear.folkmq.proxy.middleware.mq.FolkmqProxyListener;
import org.noear.folkmq.common.MqConstants;
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
public class AdminQueueController extends BaseController {
    static final Logger log = LoggerFactory.getLogger(AdminQueueController.class);

    @Inject
    FolkmqProxyListener brokerListener;

    @Inject
    ViewQueueService viewQueueService;

    @Inject
    QueueForceService queueForceService;

    @Mapping("/admin/queue")
    public ModelAndView queue() {
        List<QueueVo> list = viewQueueService.getQueueListVo();
        list.sort(Comparator.comparing(v -> v.queue));

        return view("admin_queue").put("list", list);
    }

    @Mapping("/admin/queue_session")
    public ModelAndView queue_session(@NotEmpty String topic, @NotEmpty String consumerGroup) throws IOException {
        String queueName = topic + MqConstants.SEPARATOR_TOPIC_CONSUMER_GROUP + consumerGroup;
        List<String> list = viewQueueService.getQueueSessionList(queueName);

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
        return queueForceService.forceDistribute(topic, consumerGroup);
    }

    @Post
    @Mapping("/admin/queue_details/ajax/clear")
    public Result queue_details_ajax_clear(@NotEmpty String topic, @NotEmpty String consumerGroup) {
        return queueForceService.forceClear(topic, consumerGroup);
    }

    @Post
    @Mapping("/admin/queue_details/ajax/delete")
    public Result queue_details_ajax_delete(@NotEmpty String topic, @NotEmpty String consumerGroup) {
        return queueForceService.forceDelete(topic, consumerGroup);
    }
}
