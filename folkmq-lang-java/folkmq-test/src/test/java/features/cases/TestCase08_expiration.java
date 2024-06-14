package features.cases;

import org.noear.folkmq.FolkMQ;
import org.noear.folkmq.client.MqMessage;
import org.noear.folkmq.broker.MqQueue;

import java.util.Date;

/**
 * @author noear
 * @since 1.0
 */
public class TestCase08_expiration extends BaseTestCase {
    public TestCase08_expiration(int port) {
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
                .connect();

        client.subscribe("demo", "a", ((message) -> {
        }));

        client.unsubscribe("demo", "a");

        client.publish("demo", new MqMessage("demo1")
                .expiration(new Date(System.currentTimeMillis() + 5000)));

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
