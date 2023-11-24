package demo;

import org.noear.folkmq.client.MqClient;
import org.noear.folkmq.client.MqClientImpl;

/**
 * @author noear
 * @since 1.0
 */
public class ClientDemo5 {
    public static void main(String[] args) throws Exception {

        //客户端
        MqClient client = new MqClientImpl(
                "folkmq://127.0.0.1:9393?accessKey=folkmq&accessSecretKey=YapLHTx19RlsEE16");

        //订阅
        client.subscribe("demo3", "c", message -> {
            System.out.println("ClientDemo5::" + message);
        });
    }
}
