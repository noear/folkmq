package org.noear.folkmq.embedded.server.admin;

import org.noear.folkmq.embedded.server.admin.utils.ImageUtils;
import org.noear.folkmq.embedded.server.admin.utils.RandomUtils;
import org.noear.folkmq.embedded.server.common.ConfigNames;
import org.noear.solon.Solon;
import org.noear.solon.Utils;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.annotation.Post;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.MethodType;
import org.noear.solon.core.handle.ModelAndView;
import org.noear.solon.core.handle.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * 登录控制器（不加控制器注解，方便手动管理）
 *
 * @author noear
 * @since 1.0
 */
public class LoginController extends BaseController {
    static final Logger log = LoggerFactory.getLogger(LoginController.class);

    private final String adminPassword;
    private final String adminUser = "admin";

    public LoginController(){
        adminPassword = Solon.cfg().get(ConfigNames.folkmq_admin, "");
    }

    @Mapping("/")
    public void home(Context ctx) throws Exception {
        ctx.redirect("./login");
    }

    @Mapping("/login") //视图 返回
    public ModelAndView login(Context ctx) throws Throwable {
        ctx.sessionClear();
        return view("login");
    }
    //-----------------

    //ajax.path like "{view}/ajax/{cmd}"

    @Post
    @Mapping("/login/ajax/check")
    public Result login_ajax_check(Context ctx, String userName, String passWord, String captcha) throws Exception {

        //空内容检查
        if (Utils.isEmpty(captcha)) {
            return Result.failure("提示：请输入验证码！");
        }

        if (Utils.isEmpty(userName) || Utils.isEmpty(passWord)) {
            return Result.failure("提示：请输入账号和密码！");
        }

        //验证码检查
        MDC.put("tag1", ctx.path());
        MDC.put("tag2", userName);

        String captchaOfSessoin = ctx.sessionOrDefault("Validation_String", "");
        if (captcha.equalsIgnoreCase(captchaOfSessoin) == false) {
            return Result.failure("提示：验证码错误！");
        }

        //用户登录
        if (adminUser.equals(userName) && adminPassword.equals(passWord)) {
            //1.用户登录::成功
            ctx.sessionSet("Logined", "1");

            //3.返回提示
            return Result.succeed("./admin");
        } else {
            return Result.failure("提示：账号或密码不对！");
        }
    }

    /*
     * 获取验证码图片
     */
    @Mapping(value = "/login/validation/img", method = MethodType.GET, produces = "image/jpeg")
    public void getValidationImg(Context ctx) throws IOException {
        // 生成验证码存入session
        String code = RandomUtils.code(4);
        ctx.sessionSet("Validation_String", code);
        ctx.sessionState().sessionPublish();

        // 获取图片
        BufferedImage bufferedImage = ImageUtils.getValidationImage(code);

        // 禁止图像缓存
        ctx.headerSet("Pragma", "no-cache");
        ctx.headerSet("Cache-Control", "no-cache");
        ctx.headerSet("Expires", "0");

        // 图像输出
        ImageIO.setUseCache(false);
        ImageIO.write(bufferedImage, "jpeg", ctx.outputStream());
    }
}
