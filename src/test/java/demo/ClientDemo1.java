package demo;

import org.noear.folkmq.client.MqClient;
import org.noear.folkmq.client.MqClientImpl;

/**
 * @author noear
 * @since 1.0
 */
public class ClientDemo1 {
    public static void main(String[] args) throws Exception {
        //客户端
        MqClient client = new MqClientImpl(
                "folkmq://127.0.0.1:9393?accessKey=folkmq&accessSecretKey=YapLHTx19RlsEE16");

        //订阅
        client.subscribe("demo", ((topic, message) -> {
            System.out.println("ClientDemo1::" + topic + " - " + message);
        }));

        //发布
        client.publish("demo", "hi");

        for (int i = 0; i < 10; i++) {
            Thread.sleep(100);
            client.publish("demo", "hi");
        }
    }
}
