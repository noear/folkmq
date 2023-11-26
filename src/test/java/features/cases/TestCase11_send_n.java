package features.cases;

import org.noear.folkmq.client.MqClientImpl;
import org.noear.folkmq.server.MqServerImpl;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author noear
 * @since 1.0
 */
public class TestCase11_send_n extends BaseTestCase {
    public TestCase11_send_n(int port) {
        super(port);
    }

    @Override
    public void start() throws Exception {
        super.start();

        //服务端
        server = new MqServerImpl()
                .start(getPort());

        //客户端
        CountDownLatch countDownLatch = new CountDownLatch(5);

        client = new MqClientImpl("folkmq://127.0.0.1:" + getPort())
                .connect();

        client.subscribe("demo", "a", ((message) -> {
            System.out.println(message);
            countDownLatch.countDown();
        }));

        client.publish("demo", "demo1");
        client.publish("demo", "demo2");
        client.publish("demo", "demo3");
        client.publish("demo", "demo4");
        client.publish("demo", "demo5");

        countDownLatch.await(1, TimeUnit.SECONDS);

        assert countDownLatch.getCount() == 0;

        assert true;
    }
}
