package demo;

import org.noear.folkmq.borker.MqBorker;
import org.noear.folkmq.borker.MqBorkerDefault;

/**
 * @author noear
 * @since 1.0
 */
public class ServerDemo {
    public static void main(String[] args) throws Exception {
        MqBorker server = new MqBorkerDefault()
                .addAccess("folkmq", "YapLHTx19RlsEE16")
                .start(18602);
    }
}
