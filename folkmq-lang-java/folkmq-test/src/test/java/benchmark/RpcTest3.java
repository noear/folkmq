package benchmark;

import org.noear.folkmq.FolkMQ;
import org.noear.folkmq.client.MqClient;
import org.noear.folkmq.client.MqMessage;
import org.noear.folkmq.borker.MqBorker;

import java.util.concurrent.CountDownLatch;

//单连接单线程发
public class RpcTest3 {
    public static void main(String[] args) throws Exception {
        //服务端
        MqBorker server = FolkMQ.createBorker()
                .addAccess("folkmq", "YapLHTx19RlsEE16")
                .start(18602);

        Thread.sleep(1000);

        //客户端
        int count = 100_000;
        CountDownLatch consumeDownLatch = new CountDownLatch(count * 3);
        CountDownLatch sendDownLatch = new CountDownLatch(3);

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
        Thread thread1 = new Thread(()->{
            try {
                for (int i = 0; i < count; i++) {
                    client2.send( new MqMessage("test-" + i).tag("test"), "demo-app1");
                }

                sendDownLatch.countDown();
            }catch (Exception e){
                e.printStackTrace();
            }
        });

        Thread thread2 = new Thread(()->{
            try {
                for (int i = 0; i < count; i++) {
                    client2.send( new MqMessage("test-" + i).tag("test"), "demo-app1");
                }

                sendDownLatch.countDown();
            }catch (Exception e){
                e.printStackTrace();
            }
        });

        Thread thread3 = new Thread(()->{
            try {
                for (int i = 0; i < count; i++) {
                    client2.send( new MqMessage("test-" + i).tag("test"), "demo-app1");
                }

                sendDownLatch.countDown();
            }catch (Exception e){
                e.printStackTrace();
            }
        });

        thread1.start();
        thread2.start();
        thread3.start();

        sendDownLatch.await();

        long sendTime = System.currentTimeMillis() - start_time;

        System.out.println("sendTime: " + sendTime + "ms");

        consumeDownLatch.await();

        long consumeTime = System.currentTimeMillis() - start_time;

        System.out.println("sendTime: " + sendTime + "ms");
        System.out.println("consumeTime: " + consumeTime + "ms, count: " + (count*3 - consumeDownLatch.getCount()));
    }
}
