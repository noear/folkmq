package org.noear.folkmq.broker.embedded.admin;

import org.noear.folkmq.broker.embedded.admin.model.TopicVo;
import org.noear.folkmq.client.MqMessage;
import org.noear.folkmq.common.MqUtils;
import org.noear.folkmq.broker.MqBorkerInternal;
import org.noear.snack.core.utils.DateUtil;
import org.noear.socketd.transport.core.Message;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.core.handle.ModelAndView;
import org.noear.solon.core.handle.Result;
import org.noear.solon.validation.annotation.Logined;
import org.noear.solon.validation.annotation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 管理控制器（不加控制器注解，方便手动管理）
 *
 * @author noear
 * @since 1.0
 */
@Logined
@Valid
public class AdminController extends BaseController {
    static final Logger log = LoggerFactory.getLogger(AdminController.class);

    @Inject
    MqBorkerInternal serviceInternal;

    @Mapping("/admin")
    public ModelAndView admin() {
        return view("admin");
    }

    @Mapping("/admin/stat")
    public ModelAndView stat() {
        ModelAndView mv = view("admin_stat");

        mv.put("qpsInput",serviceInternal.getQpsPublish());
        mv.put("qpsOutput",serviceInternal.getQpsDistribute());

        return mv;
    }

    @Mapping("/admin/topic")
    public ModelAndView topic() {
        Iterator<Map.Entry<String, Set<String>>> iterator = serviceInternal.getSubscribeMap().entrySet().iterator();

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

    @Mapping("/admin/publish")
    public ModelAndView publish() {
        return view("admin_publish");
    }

    @Mapping("/admin/publish/ajax/post")
    public Result publish_ajax_post(String topic, String scheduled, int qos, String content) {
        try {
            if (serviceInternal.hasSubscribe(topic)) {
                Date scheduledDate = DateUtil.parse(scheduled);
                MqMessage message = new MqMessage(content).qos(qos).scheduled(scheduledDate);
                Message routingMessage = MqUtils.getLast().routingMessageBuild(topic, message);


                serviceInternal.routingDo(MqUtils.getLast(), routingMessage);

                return Result.succeed();
            } else {
                return Result.failure("主题不存在!");
            }
        } catch (Exception e) {
            return Result.failure(e.getLocalizedMessage());
        }
    }
}