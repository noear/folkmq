package demo;

import org.noear.folkmq.FolkMQ;
import org.noear.folkmq.broker.MqBorker;

/**
 * @author noear
 * @since 1.0
 */
public class ServerDemo {
    public static void main(String[] args) throws Exception {
        MqBorker server = FolkMQ.createBorker()
                .addAccess("folkmq", "YapLHTx19RlsEE16")
                .start(18602);
    }
}
