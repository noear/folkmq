package features.cases;

import org.noear.folkmq.client.MqClientDefault;
import org.noear.folkmq.server.MqServerDefault;
import org.noear.folkmq.server.pro.MqPersistentSnapshot;

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
                .persistent(new MqPersistentSnapshot())
                .start(getPort());

        //客户端
        CountDownLatch countDownLatch = new CountDownLatch(4);

        client = new MqClientDefault("folkmq://127.0.0.1:" + getPort())
                .connect();

        client.subscribe("demo", "a", ((message) -> {
            System.out.println(message);
            countDownLatch.countDown();
        }));

        client.publish("demo", "demo0").get(); //停连前，确保发完了
        client.disconnect();

        server.stop();
        server.start(getPort());

        //上面已有有订阅记录了
        client.connect();
        client.unsubscribe("demo", "a"); //取消订阅； 为了不马上被派发掉
        client.publish("demo", "demo1").get();
        client.publish("demo", "demo2").get();

        server.stop();
        server.start(getPort()); //恢复三条数据


        //上面已有有订阅记录了（有两条记录未发了）
        client.connect();

        client.subscribe("demo", "a", ((message) -> {
            System.out.println(message);
            countDownLatch.countDown();
        }));

        client.publish("demo", "demo3");

        countDownLatch.await(10, TimeUnit.SECONDS);//持久化恢复后的数据，会自动延后

        System.out.println(countDownLatch.getCount());
        assert countDownLatch.getCount() == 0;

        assert true;
    }
}
