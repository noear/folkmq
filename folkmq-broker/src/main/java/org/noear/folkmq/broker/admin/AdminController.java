package org.noear.folkmq.broker.admin;

import org.noear.folkmq.broker.admin.dso.LicenceUtils;
import org.noear.folkmq.broker.admin.dso.ViewQueueService;
import org.noear.folkmq.broker.admin.model.QueueVo;
import org.noear.folkmq.broker.admin.model.ServerVo;
import org.noear.folkmq.broker.admin.model.TopicVo;
import org.noear.folkmq.broker.mq.BrokerListenerFolkmq;
import org.noear.folkmq.client.MqMessage;
import org.noear.folkmq.common.MqConstants;
import org.noear.folkmq.common.MqUtils;
import org.noear.folkmq.server.MqQueue;
import org.noear.snack.core.utils.DateUtil;
import org.noear.socketd.transport.core.Entity;
import org.noear.socketd.transport.core.Message;
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
import java.net.InetSocketAddress;
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
    BrokerListenerFolkmq brokerListener;

    @Inject
    ViewQueueService viewQueueService;


    @Mapping("/admin")
    public ModelAndView admin() {
        ModelAndView vm = view("admin");

        vm.put("isValid", LicenceUtils.isValid());

        if (LicenceUtils.isValid()) {
            switch (LicenceUtils.isAuthorized()) {
                case -1:
                    vm.put("licenceBtn", "非法授权");
                    break;
                case 1:
                    vm.put("licenceBtn", "正版授权");
                    break;
                default:
                    vm.put("licenceBtn", "授权检测");
                    break;

            }
        } else {
            vm.put("licenceBtn", "非法授权");
        }

        return vm;
    }

    @Mapping("/admin/licence")
    public ModelAndView licence() {
        ModelAndView vm = view("admin_licence");

        vm.put("isAuthorized", false);

        if (LicenceUtils.isValid() == false) {
            vm.put("licence", "无效许可证（请购买正版授权：<a href='https://folkmq.noear.org' target='_blank'>https://folkmq.noear.org</a>）");
            vm.put("checkBtnShow", false);
        } else {
            vm.put("licence", LicenceUtils.getLicence2());

            if (LicenceUtils.isAuthorized() == 0) {
                vm.put("checkBtnShow", true);
            } else {
                vm.put("checkBtnShow", false);

                if (LicenceUtils.isAuthorized() == 1) {
                    vm.put("isAuthorized", true);
                    vm.put("subscribeDate", LicenceUtils.getSubscribeDate());
                    vm.put("subscribeMonths", LicenceUtils.getSubscribeMonths());
                    vm.put("consumer", LicenceUtils.getConsumer());
                } else {
                    vm.put("licence", "非法授权（请购买正版授权：<a href='https://folkmq.noear.org' target='_blank'>https://folkmq.noear.org</a>）");
                }
            }
        }

        return vm;
    }

    @Mapping("/admin/licence/ajax/check")
    public Result licence_check() {
        if (LicenceUtils.isValid() == false) {
            return Result.failure(400, "无效许可证");
        }

        return LicenceUtils.auth();
    }

    @Mapping("/admin/topic")
    public ModelAndView topic() {
        Map<String, Set<String>> subscribeMap = brokerListener.getSubscribeMap();

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


            Collection<Session> tmp = brokerListener.getPlayerAll(MqConstants.BROKER_AT_SERVER);

            if (tmp != null) {
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


            Collection<Session> tmp = brokerListener.getPlayerAll(MqConstants.BROKER_AT_SERVER);

            if (tmp != null && tmp.size() > 0) {
                List<Session> serverList = new ArrayList<>(tmp);
                Entity entity = new StringEntity("")
                        .metaPut(MqConstants.MQ_META_TOPIC, topic)
                        .metaPut(MqConstants.MQ_META_CONSUMER_GROUP, consumerGroup);

                for (Session s1 : serverList) {
                    s1.send(MqConstants.ADMIN_QUEUE_FORCE_DELETE, entity);
                }
            }

            return Result.succeed();

        } catch (Throwable e) {
            return Result.failure(e.getLocalizedMessage());
        } finally {
            force_lock.set(false);
        }
    }

    @Mapping("/admin/server")
    public ModelAndView server() throws IOException {
        List<ServerVo> list = new ArrayList<>();
        Collection<Session> tmp = brokerListener.getPlayerAll(MqConstants.BROKER_AT_SERVER);

        if (tmp != null) {
            List<Session> serverList = new ArrayList<>(tmp);

            //用 list 转一下，免避线程安全
            for (Session session : serverList) {
                InetSocketAddress socketAddress = session.remoteAddress();
                String adminPort = session.param("port");
                String adminAddr = socketAddress.getAddress().toString();

                if (adminAddr.startsWith("/")) {
                    adminAddr = adminAddr.substring(1);
                }

                ServerVo serverVo = new ServerVo();
                serverVo.sid = session.sessionId();
                serverVo.addree = adminAddr + ":" + socketAddress.getPort();
                serverVo.adminUrl = "http://" + adminAddr + ":" + adminPort + "/admin";

                list.add(serverVo);

                //不超过99
                if (list.size() == 99) {
                    break;
                }
            }
        }

        return view("admin_server").put("list", list);
    }

    @Mapping("/admin/server/ajax/save")
    public Result server_save(@NotEmpty String sid) throws IOException {
        Collection<Session> tmp = brokerListener.getPlayerAll(MqConstants.BROKER_AT_SERVER);

        if (tmp != null) {
            List<Session> serverList = new ArrayList<>(tmp);
            for (Session s1 : serverList) {
                if (s1.sessionId().equals(sid)) {
                    s1.send(MqConstants.MQ_EVENT_SAVE, new StringEntity(""));
                    return Result.succeed();
                }
            }
        }

        return Result.failure("操作失败");
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

            if (brokerListener.publishDo(topic, message)) {
                return Result.succeed();
            } else {
                return Result.failure("集群没有服务节点");
            }
        } catch (Exception e) {
            return Result.failure(e.getLocalizedMessage());
        }
    }
}