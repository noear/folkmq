package demo;

import org.noear.folkmq.client.MqClient;
import org.noear.folkmq.client.MqClientDefault;

/**
 * @author noear
 * @since 1.0
 */
public class ClientDemo2 {
    public static void main(String[] args) throws Exception {

        //客户端
        MqClient client = new MqClientDefault("folkmq://127.0.0.1:18602?ak=folkmq&sk=YapLHTx19RlsEE16")
                .autoAcknowledge(false)
                .connect();

        //订阅
        //订阅
        client.subscribe("demo", "b", message -> {
            if (message.getTimes() < 2) {
                System.out.println("ClientDemo2-no::" + message);
                message.acknowledge(false);
            } else {
                System.out.println("ClientDemo2-ok::" + message);
                message.acknowledge(true);
            }
        });
    }
}
