package labs;

import org.noear.folkmq.FolkMQ;
import org.noear.folkmq.client.MqClient;
import org.noear.folkmq.client.MqMessage;

public class HaTest {
    public static void main(String[] args) throws Exception {
        //客户端
        MqClient client = FolkMQ.createClient("folkmq:ws://127.0.0.1:18603?ak=ak1&sk=sk1",
                        "folkmq:ws://127.0.0.1:18703?ak=ak1&sk=sk1")
                .nameAs("demoapp")
                .connect();

        client.subscribe("demo", (msg) -> {
            System.out.println(msg.getBodyAsString());
        });

        long i = 0;
        while (true) {
            client.publishAsync("demo", new MqMessage("test-" + (i++)));
        }
    }
}
