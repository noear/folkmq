package org.noear.folkmq.broker.embedded.admin;

import org.noear.folkmq.FolkMQ;
import org.noear.folkmq.broker.embedded.MqBrokerConfig;
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
        viewModel.put("root", MqBrokerConfig.displayPath);

        viewModel.put("static", MqBrokerConfig.displayPath);
        viewModel.put("css", MqBrokerConfig.displayPath + "/css");
        viewModel.put("js", MqBrokerConfig.displayPath + "/js");
        viewModel.put("img", MqBrokerConfig.displayPath + "/img");

        return viewModel;
    }
}