package labs;

import org.junit.jupiter.api.Test;
import org.noear.folkmq.FolkMQ;
import org.noear.folkmq.client.MqClient;
import org.noear.folkmq.client.MqMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author noear
 * @since 1.0
 */
public class BroadcastTest {
    @Test
    public void start() throws Exception {
        String serverUrl = "folkmq://127.0.0.1:18602?ak=ak1&sk=sk1";
        CountDownLatch countDownLatch = new CountDownLatch(2);

        //客户端
        MqClient client = FolkMQ.createClient(serverUrl)
                .nameAs("demo-app")
                .connect();

        client.subscribe("demo.topic", ((message) -> {
            System.out.println(message);
            countDownLatch.countDown();
        }));

        //客户端
        MqClient client2 = FolkMQ.createClient(serverUrl)
                .nameAs("demo-app")
                .connect();

        client2.subscribe("demo.topic", ((message) -> {
            System.out.println(message);
            countDownLatch.countDown();
        }));


        client.publish("demo.topic", new MqMessage("demo1").broadcast(true));

        Thread.sleep(100);

        assert countDownLatch.getCount() == 0;
    }
}