package org.noear.folkmq.broker.admin;

import org.noear.folkmq.broker.admin.dso.LicenceUtils;
import org.noear.folkmq.broker.admin.model.SessionVo;
import org.noear.socketd.broker.BrokerListener;
import org.noear.socketd.transport.core.Session;
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
    BrokerListener brokerListener;


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
}