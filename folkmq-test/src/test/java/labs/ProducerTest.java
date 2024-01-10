package labs;

import org.noear.folkmq.FolkMQ;
import org.noear.folkmq.client.MqClient;
import org.noear.folkmq.client.MqMessage;

public class ProducerTest {
    public static void main(String[] args) throws Exception {
        //客户端
        MqClient client = FolkMQ.createClient("folkmq://127.0.0.1:18602?ak=folkmq&sk=YapLHTx19RlsEE16")
                .connect();

        int count = 10_000;

        //发布预热
        for (int i = 0; i < 100; i++) {
            client.publish("hot", new MqMessage("hot-" + i));
        }

        for (int i = 0; i < count; i++) {
            client.publishAsync("test", new MqMessage("test-" + i));
        }
    }
}
