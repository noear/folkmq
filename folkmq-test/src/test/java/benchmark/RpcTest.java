package benchmark;

import org.noear.folkmq.FolkMQ;
import org.noear.folkmq.client.MqClient;
import org.noear.folkmq.client.MqMessage;
import org.noear.folkmq.server.MqServer;
import org.noear.folkmq.server.MqServerDefault;

import java.util.concurrent.CountDownLatch;

//单连接单线程发
public class RpcTest {
    public static void main(String[] args) throws Exception {
        //服务端
        MqServer server = new MqServerDefault()
                .addAccess("folkmq", "YapLHTx19RlsEE16")
                .start(18602);

        Thread.sleep(1000);

        //客户端
        int count = 100_000;
        CountDownLatch countDownLatch = new CountDownLatch(count);

        MqClient client1 = FolkMQ.createClient("folkmq://127.0.0.1:18602?ak=folkmq&sk=YapLHTx19RlsEE16")
                .nameAs("demo-app1")
                .connect();

        MqClient client2 = FolkMQ.createClient("folkmq://127.0.0.1:18602?ak=folkmq&sk=YapLHTx19RlsEE16")
                .nameAs("demo-app2")
                .connect();

        client1.listen(m->{
            if("tag".equals(m.getTag())) {
                m.acknowledge(true);
                countDownLatch.countDown();
            }
        });


        //发布测试
        long start_time = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            client2.send( new MqMessage("test-" + i).tag("test"), "demo-app1");
        }
        long sendTime = System.currentTimeMillis() - start_time;

        System.out.println("sendTime: " + sendTime + "ms");
        countDownLatch.await();

        long consumeTime = System.currentTimeMillis() - start_time;

        System.out.println("sendTime: " + sendTime + "ms");
        System.out.println("consumeTime: " + consumeTime + "ms, count: " + (count - countDownLatch.getCount()));
    }
}
