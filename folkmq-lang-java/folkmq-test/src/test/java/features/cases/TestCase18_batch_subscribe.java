package features.cases;

import org.noear.folkmq.FolkMQ;
import org.noear.folkmq.client.MqMessage;
import org.noear.folkmq.broker.MqQueue;
import org.noear.folkmq.broker.MqBorkerInternal;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author noear
 * @since 1.0
 */
public class TestCase18_batch_subscribe extends BaseTestCase {
    public TestCase18_batch_subscribe(int port) {
        super(port);
    }

    @Override
    public void start() throws Exception {
        super.start();

        //服务端
        server = FolkMQ.createBorker()
                .start(getPort());

        //客户端
        CountDownLatch countDownLatch = new CountDownLatch(1);

        client = FolkMQ.createClient("folkmq://127.0.0.1:" + getPort());



        client.subscribe("demo", "a", ((message) -> {
            System.out.println(message);
            countDownLatch.countDown();
        }));

        client.subscribe("demo2", "a", ((message) -> {
            System.out.println(message);
            countDownLatch.countDown();
        }));

        client.subscribe("demo3", "a", ((message) -> {
            System.out.println(message);
            countDownLatch.countDown();
        }));

        client.connect();

        client.publishAsync("demo",  new MqMessage("demo1"));

        countDownLatch.await(1, TimeUnit.SECONDS);

        //检验客户端
        assert countDownLatch.getCount() == 0;

        Thread.sleep(100);

        //检验服务端
        MqBorkerInternal serverInternal = server.getServerInternal();
        System.out.println("server topicConsumerMap.size=" + serverInternal.getQueueMap().size());
        assert serverInternal.getQueueMap().size() == 3;

        MqQueue topicConsumerQueue = serverInternal.getQueue("demo#a");
        System.out.println("server topicConsumerQueue.size=" + topicConsumerQueue.messageTotal());
        assert topicConsumerQueue.messageTotal() == 0;
        assert topicConsumerQueue.messageTotal2() == 0;
    }
}
