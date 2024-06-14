package features.cases;

import org.noear.folkmq.FolkMQ;
import org.noear.folkmq.client.MqMessage;

import java.util.Date;

/**
 * @author noear
 * @since 1.0
 */
public class TestCase17_unpublish extends BaseTestCase {
    public TestCase17_unpublish(int port) {
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
            System.out.println(message);
        }));

        MqMessage mqMessage = new MqMessage("demo1").scheduled(new Date(System.currentTimeMillis() + 5000));
        client.publishAsync("demo", mqMessage);

        Thread.sleep(1000);

        assert server.getServerInternal().getQueue("demo#a").messageTotal() == 1;
        assert server.getServerInternal().getQueue("demo#a").messageTotal2() == 1;

        Thread.sleep(1000);

        assert server.getServerInternal().getQueue("demo#a").messageTotal() == 1;
        assert server.getServerInternal().getQueue("demo#a").messageTotal2() == 1;

        client.unpublish("demo", mqMessage.getKey());

        Thread.sleep(10);

        assert server.getServerInternal().getQueue("demo#a").messageTotal() == 0;
        assert server.getServerInternal().getQueue("demo#a").messageTotal2() == 0;
    }
}
