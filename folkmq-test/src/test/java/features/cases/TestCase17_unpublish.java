package features.cases;

import org.noear.folkmq.client.MqClientDefault;
import org.noear.folkmq.client.MqMessage;
import org.noear.folkmq.server.MqQueue;
import org.noear.folkmq.server.MqServerDefault;
import org.noear.folkmq.server.MqServiceInternal;

import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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
        server = new MqServerDefault()
                .start(getPort());

        //客户端

        client = new MqClientDefault("folkmq://127.0.0.1:" + getPort())
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

        client.unpublish("demo", mqMessage.getTid());

        Thread.sleep(10);

        assert server.getServerInternal().getQueue("demo#a").messageTotal() == 0;
        assert server.getServerInternal().getQueue("demo#a").messageTotal2() == 0;
    }
}
