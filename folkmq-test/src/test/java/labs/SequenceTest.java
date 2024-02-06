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
public class SequenceTest {
    @Test
    public void start() throws Exception {
        //客户端
        MqClient client = FolkMQ.createClient("folkmq://127.0.0.1:18602?ak=folkmq&sk=YapLHTx19RlsEE16")
                .connect();

        //客户端
        int count = 10000;
        CountDownLatch countDownLatch = new CountDownLatch(count);


        List<Integer> msgList = new ArrayList<>();
        client.subscribe("demo", "a", ((message) -> {
            msgList.add(Integer.parseInt(message.getContent()));
            countDownLatch.countDown();
        }));

        for (int i = 0; i < count; i++) {
            client.publish("demo", new MqMessage(String.valueOf(i)).sequence(true));
        }

        countDownLatch.await(2, TimeUnit.SECONDS);

        //检验客户端
        assert countDownLatch.getCount() == 0;

        int val = 0;
        for (Integer v1 : msgList) {
            if (v1 < val) {
                assert false;
            } else {
                val = v1;
            }
        }

        assert true;
    }
}