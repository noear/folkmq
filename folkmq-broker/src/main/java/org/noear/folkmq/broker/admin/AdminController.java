package org.noear.folkmq.broker.admin;

import org.noear.folkmq.broker.admin.model.SessionVo;
import org.noear.socketd.broker.BrokerListener;
import org.noear.socketd.transport.core.Session;
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
    BrokerListener brokerListener;

    @Mapping("/admin")
    public ModelAndView admin() {
        return view("admin");
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