package features.cases;

import org.noear.folkmq.FolkMQ;
import org.noear.folkmq.client.MqClient;
import org.noear.folkmq.client.MqMessage;

import java.util.concurrent.CountDownLatch;

/**
 * @author noear
 * @since 1.0
 */
public class TestCase31_broadcast extends BaseTestCase {
    public TestCase31_broadcast(int port) {
        super(port);
    }

    private MqClient client2;

    @Override
    public void start() throws Exception {
        super.start();

        CountDownLatch countDownLatch = new CountDownLatch(2);

        //服务端
        server = FolkMQ.createServer()
                .start(getPort());

        //客户端
        client = FolkMQ.createClient("folkmq://127.0.0.1:" + getPort())
                .nameAs("demo-app")
                .connect();

        client.subscribe("demo.topic", ((message) -> {
            System.out.println(message);
            countDownLatch.countDown();
        }));

        //客户端
        client2 = FolkMQ.createClient("folkmq://127.0.0.1:" + getPort())
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

    @Override
    public void stop() throws Exception {
        super.stop();

        if (client2 != null) {
            client2.disconnect();
        }
    }
}
