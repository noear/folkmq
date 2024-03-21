package labs;

import org.junit.jupiter.api.Test;
import org.noear.folkmq.FolkMQ;
import org.noear.folkmq.client.MqClient;
import org.noear.folkmq.client.MqMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author noear
 * @since 1.0
 */
public class SendTest {
    @Test
    public void start() throws Exception {
        //客户端
        MqClient client = FolkMQ.createClient("folkmq://127.0.0.1:18602")
                .nameAs("demoapp")
                .connect();

        //客户端
        int count = 100;
        CountDownLatch countDownLatch = new CountDownLatch(count);


        List<Integer> msgList = new ArrayList<>();
        client.listen( ((message) -> {
            msgList.add(Integer.parseInt(message.getContent()));
            countDownLatch.countDown();
        }));

        for (int i = 0; i < count-1; i++) {
            client.send( new MqMessage(String.valueOf(i)),"demoapp");
        }

        Thread.sleep(100);

        long time_start = System.currentTimeMillis();
        client.send(new MqMessage(String.valueOf(10)),"demoapp");

        countDownLatch.await(10, TimeUnit.SECONDS);

        //检验客户端
        assert countDownLatch.getCount() == 0;

        System.out.println(System.currentTimeMillis() - time_start);
    }
}