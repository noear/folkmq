package features.cases;

import org.noear.folkmq.client.MqClientImpl;
import org.noear.folkmq.server.MqServerImpl;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author noear
 * @since 1.0
 */
public class TestCase03_ack_retry extends BaseTestCase {
    public TestCase03_ack_retry(int port) {
        super(port);
    }

    @Override
    public void start() throws Exception {
        super.start();

        //服务端
        server = new MqServerImpl()
                .start(getPort());

        //客户端
        CountDownLatch countDownLatch = new CountDownLatch(1);

        client = new MqClientImpl("folkmq://127.0.0.1:" + getPort())
                .autoAcknowledge(false)
                .connect();

        client.subscribe("demo", "a", ((message) -> {
            System.out.println(message);

            if(message.getTimes() > 1) {
                countDownLatch.countDown();
                message.acknowledge(true);
            }else{
                message.acknowledge(false);
            }
        }));

        client.publish("demo", "demo");

        countDownLatch.await(40, TimeUnit.SECONDS);

        assert countDownLatch.getCount() == 0;

        assert true;
    }
}
