package features.cases;

import org.noear.folkmq.FolkMQ;
import org.noear.folkmq.client.MqMessage;
import org.noear.folkmq.client.MqTransaction;
import org.noear.folkmq.server.MqQueue;
import org.noear.folkmq.server.MqServerDefault;
import org.noear.folkmq.server.MqServiceInternal;

import java.util.Date;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author noear
 * @since 1.0
 */
public class TestCase33_scheduled_tran extends BaseTestCase {
    public TestCase33_scheduled_tran(int port) {
        super(port);
    }

    @Override
    public void start() throws Exception {
        super.start();

        //服务端
        server = new MqServerDefault()
                .start(getPort());

        //客户端
        CountDownLatch countDownLatch = new CountDownLatch(5);

        client = FolkMQ.createClient("folkmq://127.0.0.1:" + getPort())
                .nameAs("demoapp")
                .connect();

        client.subscribe("demo",  ((message) -> {
            System.out.println(message);
            countDownLatch.countDown();
        }));

        MqTransaction tran = client.newTransaction();
        try {
            client.publishAsync("demo", new MqMessage("demo1").transaction(tran).scheduled(new Date(System.currentTimeMillis() + 5000)));
            client.publishAsync("demo", new MqMessage("demo2").transaction(tran).scheduled(new Date(System.currentTimeMillis() + 5000)));
            client.publishAsync("demo", new MqMessage("demo3").transaction(tran).scheduled(new Date(System.currentTimeMillis() + 5000)));
            client.publishAsync("demo", new MqMessage("demo4").transaction(tran).scheduled(new Date(System.currentTimeMillis() + 5000)));
            client.publishAsync("demo", new MqMessage("demo5").transaction(tran).scheduled(new Date(System.currentTimeMillis() + 5000)));

            tran.commit();
        }catch (Throwable e){
            tran.rollback();
        }
        countDownLatch.await(6, TimeUnit.SECONDS);

        //检验客户端
        assert countDownLatch.getCount() == 0;

        Thread.sleep(100);

        //检验服务端
        MqServiceInternal serverInternal = server.getServerInternal();
        System.out.println("server topicConsumerMap.size=" + serverInternal.getQueueMap().size());
        assert serverInternal.getQueueMap().size() == 2;

        MqQueue topicConsumerQueue = serverInternal.getQueueMap().values().toArray(new MqQueue[1])[0];
        System.out.println("server topicConsumerQueue.size=" + topicConsumerQueue.messageTotal());
        assert topicConsumerQueue.messageTotal() == 0;
        assert topicConsumerQueue.messageTotal2() == 0;
    }
}
