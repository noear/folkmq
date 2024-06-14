package org.noear.folkmq.borker.embedded.admin;

import org.noear.folkmq.FolkMQ;
import org.noear.folkmq.borker.embedded.MqServerConfig;
import org.noear.solon.core.handle.ModelAndView;

/**
 * @author noear
 * @since 1.0
 */
public abstract class BaseController {

    /*
     * @return 输出一个视图（自动放置viewModel）
     * @param viewName 视图名字(内部uri)
     * */
    public ModelAndView view(String viewName) {
        //设置必要参数
        ModelAndView viewModel = new ModelAndView("/folkmq/" + viewName + ".ftl");


        viewModel.put("title", "FolkMQ");
        viewModel.put("app", "FolkMQ");
        viewModel.put("version", FolkMQ.versionName());
        viewModel.put("root", MqServerConfig.path);

        viewModel.put("static", "/folkmq");
        viewModel.put("css", "/folkmq/css");
        viewModel.put("js", "/folkmq/js");
        viewModel.put("img", "/folkmq/img");

        return viewModel;
    }
}