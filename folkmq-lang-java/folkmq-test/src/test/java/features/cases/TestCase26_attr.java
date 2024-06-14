package features.cases;

import org.noear.folkmq.FolkMQ;
import org.noear.folkmq.client.MqMessage;
import org.noear.folkmq.borker.MqQueue;
import org.noear.folkmq.borker.MqBorkerInternal;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author noear
 * @since 1.0
 */
public class TestCase26_attr extends BaseTestCase {
    public TestCase26_attr(int port) {
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

        client = FolkMQ.createClient("folkmq://127.0.0.1:" + getPort())
                .config(c->c.metaPut("ak","").metaPut("sk",""))
                .connect();

        client.subscribe("demo", "a", ((message) -> {
            System.out.println(message);
            if("1".equals(message.getAttr("a1"))) {
                countDownLatch.countDown();
            }
        }));

        client.publishAsync("demo",  new MqMessage("demo1").attr("a1","1"));

        countDownLatch.await(1, TimeUnit.SECONDS);

        //检验客户端
        assert countDownLatch.getCount() == 0;

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
