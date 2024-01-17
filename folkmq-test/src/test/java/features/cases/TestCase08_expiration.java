package features.cases;

import org.noear.folkmq.client.MqClientDefault;
import org.noear.folkmq.client.MqMessage;
import org.noear.folkmq.server.MqServerDefault;

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
        server = new MqServerDefault()
                .start(getPort());

        //客户端
        client = new MqClientDefault("folkmq://127.0.0.1:" + getPort())
                .connect();

        client.subscribe("demo", "a", ((message) -> {
        }));

        client.unsubscribe("demo", "a");

        client.publish("demo", new MqMessage("demo1")
                .expiration(new Date(System.currentTimeMillis() + 5000)));

        assert server.getServerInternal().getQueueMap().get("demo#a").messageTotal() == 1L;

        Thread.sleep(3000);

        assert server.getServerInternal().getQueueMap().get("demo#a").messageTotal() == 1L;

        Thread.sleep(3000);

        assert server.getServerInternal().getQueueMap().get("demo#a").messageTotal() == 0L;
    }
}
