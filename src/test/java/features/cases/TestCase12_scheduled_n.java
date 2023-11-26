package features.cases;

import org.noear.folkmq.client.MqClientImpl;
import org.noear.folkmq.server.MqServerImpl;

import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author noear
 * @since 1.0
 */
public class TestCase12_scheduled_n extends BaseTestCase {
    public TestCase12_scheduled_n(int port) {
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

        client.publish("demo", "demo1", new Date(System.currentTimeMillis() + 5000));
        client.publish("demo", "demo2", new Date(System.currentTimeMillis() + 5000));
        client.publish("demo", "demo3", new Date(System.currentTimeMillis() + 5000));
        client.publish("demo", "demo4", new Date(System.currentTimeMillis() + 5000));
        client.publish("demo", "demo5", new Date(System.currentTimeMillis() + 5000));

        countDownLatch.await(6, TimeUnit.SECONDS);

        assert countDownLatch.getCount() == 0;

        assert true;
    }
}
