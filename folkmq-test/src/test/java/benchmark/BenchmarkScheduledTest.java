package benchmark;

import org.noear.folkmq.client.MqClient;
import org.noear.folkmq.client.MqClientDefault;
import org.noear.folkmq.server.MqServer;
import org.noear.folkmq.server.MqServerDefault;

import java.util.Date;
import java.util.concurrent.CountDownLatch;

public class BenchmarkScheduledTest {
    public static void main(String[] args) throws Exception {
        //服务端
        MqServer server = new MqServerDefault()
                .addAccess("folkmq", "YapLHTx19RlsEE16")
                .start(9393);

        Thread.sleep(1000);
        int count = 100_000;
        CountDownLatch countDownLatch = new CountDownLatch(count);

        //客户端
        MqClient client = new MqClientDefault("folkmq://127.0.0.1:9393?ak=folkmq&sk=YapLHTx19RlsEE16")
                .connect();

        //订阅
        client.subscribe("hot", "a", message -> {
            //System.out.println("::" + topic + " - " + message);
        });

        client.subscribe("test", "a", message -> {
            //System.out.println("::" + topic + " - " + message);
            countDownLatch.countDown();
        });

        //发布预热
        for (int i = 0; i < 100; i++) {
            client.publish("hot", "hot-" + i).get();
        }

        //发布测试
        long start_time = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            client.publish("test", "test-" + i, new Date(System.currentTimeMillis() + 5000));
        }
        long sendTime = System.currentTimeMillis() - start_time;

        System.out.println("sendTime: " + sendTime);
        countDownLatch.await();

        long consumeTime = System.currentTimeMillis() - start_time;

        System.out.println("sendTime: " + sendTime);
        System.out.println("consumeTime: " + consumeTime + ", count: " + (count - countDownLatch.getCount()));
    }
}
