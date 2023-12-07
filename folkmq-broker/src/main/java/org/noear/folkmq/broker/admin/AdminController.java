package org.noear.folkmq.broker.admin;

import org.noear.folkmq.broker.admin.dso.LicenceUtils;
import org.noear.folkmq.broker.admin.dso.ViewQueueService;
import org.noear.folkmq.broker.admin.model.QueueVo;
import org.noear.folkmq.broker.admin.model.ServerVo;
import org.noear.folkmq.broker.admin.model.SessionVo;
import org.noear.folkmq.broker.admin.model.TopicVo;
import org.noear.folkmq.broker.mq.BrokerListenerFolkmq;
import org.noear.folkmq.common.MqConstants;
import org.noear.socketd.transport.core.Session;
import org.noear.solon.Utils;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.core.handle.ModelAndView;
import org.noear.solon.core.handle.Result;
import org.noear.solon.validation.annotation.Logined;
import org.noear.solon.validation.annotation.Valid;

import java.io.IOException;
import java.net.InetSocketAddress;
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
            vm.put("licenceBtn", "无效授权");
        }

        return vm;
    }

    @Mapping("/admin/licence")
    public ModelAndView licence() {
        ModelAndView vm = view("admin_licence");

        vm.put("isAuthorized", false);

        if (LicenceUtils.isValid() == false) {
            vm.put("licence", "没有或无效许可证（请购买正版授权：<a href='https://folkmq.noear.org' target='_blank'>https://folkmq.noear.org</a>）");
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
            return Result.failure(400, "没有或无效许可证");
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

    @Mapping("/admin/session")
    public ModelAndView session() {
        List<String> nameList = new ArrayList<>(brokerListener.getNameAll());
        nameList.sort(String::compareTo);

        List<SessionVo> list = new ArrayList<>();

        //用 list 转一下，免避线程安全
        for (String name : nameList) {
            Collection<Session> sessions = brokerListener.getPlayerAll(name);

            SessionVo sessionVo = new SessionVo();
            sessionVo.setName(name);
            sessionVo.setSessionCount(sessions.size());

            list.add(sessionVo);

            //不超过99
            if (list.size() == 99) {
                break;
            }
        }

        return view("admin_session").put("list", list);
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
                String admimPort = session.param("port");

                ServerVo serverVo = new ServerVo();
                serverVo.addree = socketAddress.toString();

                if (Utils.isEmpty(admimPort)) {
                    serverVo.adminUrl = "http://" + socketAddress.getHostName() + ":" + admimPort + "/admin";
                } else {
                    serverVo.adminUrl = "http://" + socketAddress.getHostName() + ":" + admimPort + "/admin";
                }

                list.add(serverVo);

                //不超过99
                if (list.size() == 99) {
                    break;
                }
            }
        }

        return view("admin_server").put("list", list);
    }
}