package demo;

import org.noear.folkmq.client.MqClient;
import org.noear.folkmq.client.MqClientImpl;

/**
 * @author noear
 * @since 1.0
 */
public class ClientDemo4 {
    public static void main(String[] args) throws Exception {

        //客户端
        MqClient client = new MqClientImpl(
                "folkmq://127.0.0.1:9393?accessKey=folkmq&accessSecretKey=no");

        //订阅
        client.subscribe("demo2", ((topic, message) -> {
            System.out.println("ClientDemo3::" + topic + " - " + message);
        }));
    }
}
