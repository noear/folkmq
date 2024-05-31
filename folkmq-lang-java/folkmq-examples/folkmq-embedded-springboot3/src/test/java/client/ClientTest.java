package client;

import org.noear.folkmq.FolkMQ;
import org.noear.folkmq.client.MqClient;
import org.noear.folkmq.client.MqMessage;

public class ClientTest {
    public static void main(String[] args) throws Exception {
        MqClient client = FolkMQ.createClient("folkmq://localhost:18080")
                .nameAs("demoapp")
                .connect();

        client.subscribe("demo.topic", message -> {
            System.out.println(message);
        });

        for (int i = 0; i < 10; i++) {
            client.publish("demo.topic", new MqMessage("hello" + i));
        }
    }
}
