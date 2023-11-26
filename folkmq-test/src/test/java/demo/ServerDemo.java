package demo;

import org.noear.folkmq.server.MqServer;
import org.noear.folkmq.server.MqServerImpl;

/**
 * @author noear
 * @since 1.0
 */
public class ServerDemo {
    public static void main(String[] args) throws Exception {
        MqServer server = new MqServerImpl()
                .addAccess("folkmq", "YapLHTx19RlsEE16")
                .start(9393);
    }
}
