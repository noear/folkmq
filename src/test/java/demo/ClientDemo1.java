package demo;

import org.noear.folkmq.client.MqClient;
import org.noear.folkmq.client.MqClientImpl;
import org.noear.folkmq.client.MqSubscription;

import java.util.Date;

/**
 * @author noear
 * @since 1.0
 */
public class ClientDemo1 {
    public static void main(String[] args) throws Exception {
        //客户端
        MqClient client = new MqClientImpl("folkmq://127.0.0.1:9393?ak=folkmq&sk=YapLHTx19RlsEE16")
                .connect();

        //订阅
        client.subscribe("demo", "a", ((message) -> {
            System.out.println("ClientDemo1::" + message);
        }));

        //发布
        for (int i = 0; i < 10; i++) {
            Thread.sleep(100);
            client.publish("demo", "hi-" + i);
            client.publish("demo2", "hi-" + i);
        }

        //延迟五秒
        client.publish("demo3", "hi-d", new Date(System.currentTimeMillis() + 5000));
    }
}
