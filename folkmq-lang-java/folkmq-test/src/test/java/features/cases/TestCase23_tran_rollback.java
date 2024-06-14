package features.cases;

import org.noear.folkmq.FolkMQ;
import org.noear.folkmq.client.MqMessage;
import org.noear.folkmq.client.MqTransaction;
import org.noear.folkmq.common.MqConstants;
import org.noear.folkmq.borker.MqQueue;
import org.noear.folkmq.borker.MqBorkerDefault;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author noear
 * @since 1.2
 */
public class TestCase23_tran_rollback extends BaseTestCase {
    public TestCase23_tran_rollback(int port) {
        super(port);
    }

    @Override
    public void start() throws Exception {
        super.start();

        //服务端
        server = new MqBorkerDefault()
                .start(getPort());

        //客户端
        CountDownLatch countDownLatch = new CountDownLatch(2);

        client = FolkMQ.createClient("folkmq://127.0.0.1:" + getPort())
                .nameAs("demoapp")
                .config(c -> c.metaPut("ak", "").metaPut("sk", ""))
                .connect();

        client.subscribe("demo", "a", ((message) -> {
            System.out.println(message);
            countDownLatch.countDown();
        }));


        MqTransaction tran = client.newTransaction();
        try {
            client.publish("demo", new MqMessage("demo1").transaction(tran));
            client.publish("demo", new MqMessage("demo2").transaction(tran));

            tran.rollback();
        } catch (Throwable e) {
            tran.rollback();
        }


        countDownLatch.await(1, TimeUnit.SECONDS);

        //检验客户端
        assert countDownLatch.getCount() == 2;

        MqQueue queue = server.getServerInternal().getQueue("demo#" + MqConstants.MQ_TRAN_CONSUMER_GROUP);
        assert queue != null;
        System.out.println(queue.messageTotal());
        assert queue.messageTotal() == 0L;
    }
}
