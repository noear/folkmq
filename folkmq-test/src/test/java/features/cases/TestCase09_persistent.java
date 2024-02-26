package features.cases;

import org.noear.folkmq.FolkMQ;
import org.noear.folkmq.client.MqClientDefault;
import org.noear.folkmq.client.MqMessage;
import org.noear.folkmq.server.MqServerDefault;
import org.noear.folkmq.server.MqServiceInternal;
import org.noear.folkmq.server.MqQueue;
import org.noear.folkmq.server.pro.MqWatcherSnapshot;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author noear
 * @since 1.0
 */
public class TestCase09_persistent extends BaseTestCase {
    public TestCase09_persistent(int port) {
        super(port);
    }

    @Override
    public void start() throws Exception {
        super.start();

        //服务端
        server = new MqServerDefault()
                .watcher(new MqWatcherSnapshot())
                .start(getPort());

        //客户端
        CountDownLatch countDownLatch = new CountDownLatch(4);

        client = FolkMQ.createClient("folkmq://127.0.0.1:" + getPort())
                .connect();

        client.subscribe("demo", "127.0.0.1", ((message) -> {
            System.out.println("::" + message);
            countDownLatch.countDown();
        }));

        client.publish("demo", new MqMessage("demo0")); //停连前，确保发完了
        Thread.sleep(100);//确保上面的消费完成
        client.disconnect();
        Thread.sleep(100);//确保断连

        server.stop();
        server = new MqServerDefault() //相当于服务器重启了
                .watcher(new MqWatcherSnapshot())
                .start(getPort());

        //上面已有有订阅记录了
        client.connect(); //新的会话
        client.unsubscribe("demo", "127.0.0.1"); //取消订阅； 为了不马上被派发掉
        Thread.sleep(100); //确保完成取消订阅了
        client.publish("demo", new MqMessage("demo1"));
        client.publish("demo", new MqMessage("demo2"));

        Thread.sleep(100);//确保断连

        server.stop();
        server = new MqServerDefault() //相当于服务器重启了
                .watcher(new MqWatcherSnapshot())
                .start(getPort());


        //上面已有有订阅记录了（有两条记录未发了）
        client.connect(); //新的会话

        client.subscribe("demo", "127.0.0.1", ((message) -> {
            System.out.println("::" + message);
            countDownLatch.countDown();
        }));

        client.publish("demo", new MqMessage("demo3"));

        Thread.sleep(100);

        countDownLatch.await(45, TimeUnit.SECONDS);//持久化恢复后的数据，会自动延后


        //检验客户端
        System.out.println(countDownLatch.getCount());
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
}
