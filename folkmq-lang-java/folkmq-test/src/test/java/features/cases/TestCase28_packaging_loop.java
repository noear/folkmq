package features.cases;

import org.noear.folkmq.FolkMQ;
import org.noear.folkmq.client.MqConsumeHandler;
import org.noear.folkmq.client.MqMessage;
import org.noear.folkmq.client.MqMessageReceived;
import org.noear.folkmq.server.MqQueue;
import org.noear.folkmq.server.MqServerDefault;
import org.noear.folkmq.server.MqServiceInternal;
import org.noear.folkmq.utils.PackagingLoop;
import org.noear.folkmq.utils.PackagingLoopImpl;
import org.noear.folkmq.utils.PackagingWorkHandler;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author noear
 * @since 1.0
 */
public class TestCase28_packaging_loop extends BaseTestCase {
    public TestCase28_packaging_loop(int port) {
        super(port);
    }

    final int count = 6;
    final CountDownLatch countDownLatch = new CountDownLatch(count);

    @Override
    public void start() throws Exception {
        super.start();

        //服务端
        server = new MqServerDefault()
                .start(getPort());

        //客户端


        client = FolkMQ.createClient("folkmq://127.0.0.1:" + getPort())
                .config(c -> c.metaPut("ak", "").metaPut("sk", ""))
                .connect();

        client.subscribe("demo", "a", new MqConsumeHandlerImpl());

        for (int i = 0; i < count; i++) {
            client.publishAsync("demo", new MqMessage("demo-" + i));
        }

        countDownLatch.await(2, TimeUnit.SECONDS);

        //检验客户端
        System.out.println("count=" + countDownLatch.getCount());
        assert countDownLatch.getCount() == 0;

        Thread.sleep(100);

        //检验服务端
        MqServiceInternal serverInternal = server.getServerInternal();
        System.out.println("server topicConsumerMap.size=" + serverInternal.getQueueMap().size());
        assert serverInternal.getQueueMap().size() == 1;

        MqQueue topicConsumerQueue = serverInternal.getQueueMap().values().toArray(new MqQueue[1])[0];
        System.out.println("server topicConsumerQueue.size=" + topicConsumerQueue.messageTotal());
        assert topicConsumerQueue.messageTotal() == 0;
        assert topicConsumerQueue.messageTotal2() == 0;
    }

    private class MqConsumeHandlerImpl implements MqConsumeHandler, PackagingWorkHandler<MqMessageReceived> {

        private PackagingLoop<MqMessageReceived> packagingLoop = new PackagingLoopImpl<>(100,3,this);


        @Override
        public void consume(MqMessageReceived message) throws Exception {
            packagingLoop.add(message);
        }

        @Override
        public void doWork(List<MqMessageReceived> list) throws Exception {
            //开始做批量处理
            System.out.println("doWork::" + list.size());

            for (MqMessageReceived m : list) {
                countDownLatch.countDown();
            }
        }
    }
}
