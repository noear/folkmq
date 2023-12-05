package features.cases;

import org.noear.folkmq.client.MqClientDefault;
import org.noear.folkmq.client.MqMessage;
import org.noear.folkmq.server.MqServer;
import org.noear.folkmq.server.MqServerDefault;
import org.noear.folkmq.server.MqServiceInternal;
import org.noear.folkmq.server.MqTopicConsumerQueue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author noear
 * @since 1.0
 */
public class TestCase16_ack_retry_multi_server extends BaseTestCase {
    public TestCase16_ack_retry_multi_server(int port) {
        super(port);
    }

    @Override
    public void start() throws Exception {
        super.start();

        //服务端
        server = new MqServerDefault()
                .start(getPort());

        MqServer server2 = new MqServerDefault()
                .start(getPort() + 10000);

        //客户端
        CountDownLatch countDownLatch = new CountDownLatch(1);

        client = new MqClientDefault("folkmq://127.0.0.1:" + getPort() +",folkmq://127.0.0.1:" + (10000+getPort()))
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

        client.publishAsync("demo", new MqMessage("demo1"));
        client.publishAsync("demo", new MqMessage("demo2"));
        client.publishAsync("demo", new MqMessage("demo3"));
        client.publishAsync("demo", new MqMessage("demo4"));

        countDownLatch.await(40, TimeUnit.SECONDS);

        //检验客户端
        assert countDownLatch.getCount() == 0;

        Thread.sleep(100);

        //检验服务端
        MqServiceInternal serverInternal = server.getServerInternal();
        System.out.println("server topicConsumerMap.size=" + serverInternal.getTopicConsumerMap().size());
        assert serverInternal.getTopicConsumerMap().size() == 1;

        MqTopicConsumerQueue topicConsumerQueue = serverInternal.getTopicConsumerMap().values().toArray(new MqTopicConsumerQueue[1])[0];
        System.out.println("server topicConsumerQueue.size=" + topicConsumerQueue.messageTotal());
        assert topicConsumerQueue.messageTotal() == 0;
        assert topicConsumerQueue.messageTotal2() == 0;



        //检验服务端
        MqServiceInternal serverInternal2 = server2.getServerInternal();
        System.out.println("server topicConsumerMap.size=" + serverInternal2.getTopicConsumerMap().size());
        assert serverInternal2.getTopicConsumerMap().size() == 1;

        MqTopicConsumerQueue topicConsumerQueue2 = serverInternal2.getTopicConsumerMap().values().toArray(new MqTopicConsumerQueue[1])[0];
        System.out.println("server topicConsumerQueue.size=" + topicConsumerQueue2.messageTotal());
        assert topicConsumerQueue2.messageTotal() == 0;
        assert topicConsumerQueue2.messageTotal2() == 0;
    }
}
