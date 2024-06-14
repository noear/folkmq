package features.cases;

import org.noear.folkmq.FolkMQ;
import org.noear.folkmq.client.MqMessage;
import org.noear.folkmq.client.MqTransaction;
import org.noear.folkmq.borker.MqQueue;

import java.util.Date;

/**
 * @author noear
 * @since 1.0
 */
public class TestCase32_expiration_tran extends BaseTestCase {
    public TestCase32_expiration_tran(int port) {
        super(port);
    }

    @Override
    public void start() throws Exception {
        super.start();

        //服务端
        server = FolkMQ.createBorker()
                .start(getPort());

        //客户端
        client = FolkMQ.createClient("folkmq://127.0.0.1:" + getPort())
                .nameAs("demoapp")
                .connect();

        client.subscribe("demo", "a", ((message) -> {
        }));

        client.unsubscribe("demo", "a");

        MqTransaction tran = client.newTransaction();
        try {
            client.publish("demo", new MqMessage("demo1")
                    .transaction(tran)
                    .expiration(new Date(System.currentTimeMillis() + 5000)));

            tran.commit();
        } catch (Throwable e) {
            tran.rollback();
        }

        MqQueue queue = server.getServerInternal().getQueue("demo#a");

        System.out.println(queue.messageTotal());
        assert queue.messageTotal() == 1L;

        Thread.sleep(3000);

        System.out.println(queue.messageTotal());
        assert queue.messageTotal() == 1L;

        Thread.sleep(3000);

        System.out.println(queue.messageTotal());
        assert queue.messageTotal() == 0L;
    }
}
