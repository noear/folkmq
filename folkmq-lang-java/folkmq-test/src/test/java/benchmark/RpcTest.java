package benchmark;

import org.noear.folkmq.FolkMQ;
import org.noear.folkmq.client.MqClient;
import org.noear.folkmq.client.MqMessage;
import org.noear.folkmq.broker.MqBorker;

import java.util.concurrent.CountDownLatch;

//单连接单线程发
public class RpcTest {
    public static void main(String[] args) throws Exception {
        //服务端
        MqBorker server = FolkMQ.createBorker()
                .addAccess("folkmq", "YapLHTx19RlsEE16")
                .start(18602);

        Thread.sleep(1000);

        //客户端
        int count = 100_000;
        CountDownLatch consumeDownLatch = new CountDownLatch(count);

        MqClient client1 = FolkMQ.createClient("folkmq://127.0.0.1:18602?ak=folkmq&sk=YapLHTx19RlsEE16")
                .nameAs("demo-app1")
                .connect();

        client1.listen(m->{
            if("test".equals(m.getTag())) {
                m.response(null);
                consumeDownLatch.countDown();
            }
        });

        MqClient client2 = FolkMQ.createClient("folkmq://127.0.0.1:18602?ak=folkmq&sk=YapLHTx19RlsEE16")
                .nameAs("demo-app2")
                .connect();

        //预热
        for (int i = 0; i < 100; i++) {
            client2.send( new MqMessage("hot-" + i).tag("hot"), "demo-app1");
        }

        //发布测试
        long start_time = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            client2.send( new MqMessage("test-" + i).tag("test"), "demo-app1");
        }
        long sendTime = System.currentTimeMillis() - start_time;

        System.out.println("sendTime: " + sendTime + "ms");
        consumeDownLatch.await();

        long consumeTime = System.currentTimeMillis() - start_time;

        System.out.println("sendTime: " + sendTime + "ms");
        System.out.println("consumeTime: " + consumeTime + "ms, count: " + (count - consumeDownLatch.getCount()));
    }
}
