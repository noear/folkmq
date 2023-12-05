package org.noear.folkmq.broker.admin;

import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.core.handle.ModelAndView;
import org.noear.solon.validation.annotation.Logined;
import org.noear.solon.validation.annotation.Valid;

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
    @Mapping("/admin")
    public ModelAndView admin() {
        return view("admin");
    }

}