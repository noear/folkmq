package features.cases;

import org.noear.folkmq.FolkMQ;
import org.noear.folkmq.client.MqMessage;
import org.noear.folkmq.borker.MqQueue;

import java.util.Date;

/**
 * @author noear
 * @since 1.0
 */
public class TestCase30_namespace extends BaseTestCase {
    public TestCase30_namespace(int port) {
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
                .nameAs("demo-app")
                .namespaceAs("DEFAULT")
                .connect();

        client.subscribe("demo.topic",  ((message) -> {
            System.out.println(message);
        }));


        client.publish("demo.topic",  new MqMessage("demo1"));
        client.publish("demo.topic",  new MqMessage("demo1").scheduled(new Date(System.currentTimeMillis() + 1_000)));

        Thread.sleep(100);

        MqQueue queue = server.getServerInternal().getQueue("DEFAULT:demo.topic#demo-app");

        System.out.println(queue.messageTotal());
        assert queue.messageTotal() == 1L;

        Thread.sleep(1000);

        System.out.println(queue.messageTotal());
        assert queue.messageTotal() == 0L;
    }
}
