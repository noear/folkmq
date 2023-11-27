package org.noear.folkmq.server.pro.admin;

import org.noear.solon.annotation.Singleton;
import org.noear.solon.core.handle.ModelAndView;

/**
 * @author noear
 * @since 1.0
 */
@Singleton(false)
public abstract class BaseController {

    /*
     * @return 输出一个视图（自动放置viewModel）
     * @param viewName 视图名字(内部uri)
     * */
    public ModelAndView view(String viewName) {
        //设置必要参数
        ModelAndView viewModel = new ModelAndView("/" + viewName + ".ftl");

        viewModel.put("root", "");

        viewModel.put("title", "FolkMQ");
        viewModel.put("app", "FolkMQ");
        viewModel.put("_version", "1.0.6");

        viewModel.put("css", "/_static/css");
        viewModel.put("js", "/_static/js");
        viewModel.put("img", "/_static/img");

        return viewModel;
    }
}