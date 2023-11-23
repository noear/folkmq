package benchmark;

import org.noear.folkmq.client.MqClient;
import org.noear.folkmq.client.MqClientImpl;
import org.noear.folkmq.client.Subscription;
import org.noear.folkmq.server.MqServer;
import org.noear.folkmq.server.MqServerImpl;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author noear 2023/11/21 created
 */
public class BenchmarkTest {
    public static void main(String[] args) throws Exception {
        //服务端
        MqServer server = new MqServerImpl()
                .addAccess("folkmq", "YapLHTx19RlsEE16")
                .start(9393);

        Thread.sleep(1000);
        int count = 10_0000 + 10000;
        CountDownLatch countDownLatch = new CountDownLatch(count);

        //客户端
        MqClient client = new MqClientImpl(
                "folkmq://127.0.0.1:9393?accessKey=folkmq&accessSecretKey=YapLHTx19RlsEE16");

        //订阅
        client.subscribe("demo", new Subscription("a", ((topic, message) -> {
            //System.out.println("ClientDemo1::" + topic + " - " + message);
            countDownLatch.countDown();
        })));

        //预热
        for (int i = 0; i < 10000; i++) {
            client.publish("demo", "hi-" + i);
        }

        //发布测试
        long start_time = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            client.publish("demo", "hi-" + i);
        }
        long sendTime = System.currentTimeMillis() - start_time;

        System.out.println("sendTime: " + sendTime);
        countDownLatch.await(6, TimeUnit.SECONDS);

        long distributeTime = System.currentTimeMillis() - start_time;

        System.out.println("distributeTime: " + distributeTime + ", count: " + (count - countDownLatch.getCount()));
    }
}
