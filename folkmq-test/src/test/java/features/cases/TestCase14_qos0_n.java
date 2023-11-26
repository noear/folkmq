package features.cases;

import org.noear.folkmq.client.MqClientDefault;
import org.noear.folkmq.server.MqServerDefault;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author noear
 * @since 1.0
 */
public class TestCase14_qos0_n extends BaseTestCase {
    public TestCase14_qos0_n(int port) {
        super(port);
    }

    @Override
    public void start() throws Exception {
        super.start();

        //服务端
        server = new MqServerDefault()
                .start(getPort());

        //客户端
        CountDownLatch countDownLatch0 = new CountDownLatch(5);
        CountDownLatch countDownLatch = new CountDownLatch(5);

        client = new MqClientDefault("folkmq://127.0.0.1:" + getPort())
                .autoAcknowledge(false)
                .connect();

        client.subscribe("demo", "a", ((message) -> {
            System.out.println(message);
            countDownLatch0.countDown();

            if (message.getTimes() > 1) {
                countDownLatch.countDown();
                message.acknowledge(true);
            } else {
                message.acknowledge(false);
            }
        }));

        client.publish("demo", "demo1", 0);
        client.publish("demo", "demo2", 0);
        client.publish("demo", "demo3", 0);
        client.publish("demo", "demo4", 0);
        client.publish("demo", "demo5", 0);

        countDownLatch.await(40, TimeUnit.SECONDS);

        assert countDownLatch.getCount() == 5;
        assert countDownLatch0.getCount() == 0;

        assert true;
    }
}