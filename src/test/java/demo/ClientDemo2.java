package demo;

import org.noear.folkmq.client.MqClient;
import org.noear.folkmq.client.MqClientImpl;

/**
 * @author noear
 * @since 1.0
 */
public class ClientDemo2 {
    public static void main(String[] args) throws Exception {

        //客户端
        MqClient client = new MqClientImpl("sd:tcp://127.0.0.1:9393");

        //订阅
        client.subscribe("demo", ((topic, message) -> {
            System.out.println("ClientDemo2::" + topic + " - " + message);
        }));
    }
}
