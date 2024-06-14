package features.cases;

import org.noear.folkmq.FolkMQ;
import org.noear.folkmq.client.MqMessage;
import org.noear.folkmq.broker.MqBorkerInternal;
import org.noear.folkmq.broker.MqQueue;

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
        server = FolkMQ.createBorker()
                .start(getPort());

        //客户端
        CountDownLatch countDownLatch0 = new CountDownLatch(5);
        CountDownLatch countDownLatch = new CountDownLatch(5);

        client = FolkMQ.createClient("folkmq://127.0.0.1:" + getPort())
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

        client.publishAsync("demo", new MqMessage("demo1").qos(0));
        client.publishAsync("demo", new MqMessage("demo2").qos(0));
        client.publishAsync("demo", new MqMessage("demo3").qos(0));
        client.publishAsync("demo", new MqMessage("demo4").qos(0));
        client.publishAsync("demo", new MqMessage("demo5").qos(0));

        countDownLatch.await(40, TimeUnit.SECONDS);

        //检验客户端
        assert countDownLatch.getCount() == 5;
        assert countDownLatch0.getCount() == 0;

        Thread.sleep(100);

        //检验服务端
        MqBorkerInternal serverInternal = server.getServerInternal();
        System.out.println("server topicConsumerMap.size=" + serverInternal.getQueueMap().size());
        assert serverInternal.getQueueMap().size() == 1;

        MqQueue topicConsumerQueue = serverInternal.getQueueMap().values().toArray(new MqQueue[1])[0];
        System.out.println("server topicConsumerQueue.size=" + topicConsumerQueue.messageTotal());
        assert topicConsumerQueue.messageTotal() == 0;
        assert topicConsumerQueue.messageTotal2() == 0;
    }
}
