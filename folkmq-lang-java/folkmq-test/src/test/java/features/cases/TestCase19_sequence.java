package features.cases;

import org.noear.folkmq.FolkMQ;
import org.noear.folkmq.client.MqMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author noear
 * @since 1.0
 */
public class TestCase19_sequence extends BaseTestCase {
    public TestCase19_sequence(int port) {
        super(port);
    }

    @Override
    public void start() throws Exception {
        super.start();

        //服务端
        server = FolkMQ.createBorker()
                .start(getPort());

        //客户端
        int count = 100_000;
        CountDownLatch countDownLatch = new CountDownLatch(count);

        client = FolkMQ.createClient("folkmq://127.0.0.1:" + getPort())
                .connect();

        List<Integer> msgList = new ArrayList<>();
        client.subscribe("demo", "a", ((message) -> {
            msgList.add(Integer.parseInt(message.getBodyAsString()));
            countDownLatch.countDown();
        }));

        for (int i = 0; i < count; i++) {
            client.publish("demo", new MqMessage(String.valueOf(i)).sequence(true));
        }

        countDownLatch.await(4, TimeUnit.SECONDS);

        //检验客户端
        if(countDownLatch.getCount() > 0) {
            System.out.println("还有未收：" + countDownLatch.getCount());
        }



        //检验客户端
        assert countDownLatch.getCount() == 0;

        int val = 0;
        for (Integer v1 : msgList) {
            if (v1 < val) {
                System.out.println(v1);
                assert false;
            } else {
                val = v1;
            }
        }

        assert true;
    }
}