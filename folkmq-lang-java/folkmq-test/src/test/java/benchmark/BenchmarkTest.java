package benchmark;

import org.noear.folkmq.FolkMQ;
import org.noear.folkmq.client.MqClient;
import org.noear.folkmq.client.MqMessage;
import org.noear.folkmq.broker.MqBorker;

import java.util.concurrent.CountDownLatch;

//单连接单线程发
public class BenchmarkTest {
    public static void main(String[] args) throws Exception {
        //服务端
        MqBorker server = FolkMQ.createBorker()
                .addAccess("folkmq", "YapLHTx19RlsEE16")
                .start(18602);

        Thread.sleep(1000);

        //客户端
        int count = 100_000;
        CountDownLatch countDownLatch = new CountDownLatch(count);

        MqClient client = FolkMQ.createClient("folkmq://127.0.0.1:18602?ak=folkmq&sk=YapLHTx19RlsEE16")
                .config(c->c.trafficLimiter(null))
                .connect();

        //订阅
        client.subscribe("hot", "demo", message -> {
            //System.out.println("::" +  message.getBodyAsString());
        });

        client.subscribe("test", "test", message -> {
            //System.out.println("::" +  message.getBodyAsString());
            countDownLatch.countDown();
        });

        //发布预热
        for (int i = 0; i < 100; i++) {
            client.publish("hot", new MqMessage("hot-" + i));
        }

        //发布测试
        long start_time = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            client.publishAsync("test", new MqMessage("test-" + i));
        }
        long sendTime = System.currentTimeMillis() - start_time;

        System.out.println("sendTime: " + sendTime + "ms");
        countDownLatch.await();

        long consumeTime = System.currentTimeMillis() - start_time;

        System.out.println("sendTime: " + sendTime + "ms");
        System.out.println("consumeTime: " + consumeTime + "ms, count: " + (count - countDownLatch.getCount()));
    }
}
