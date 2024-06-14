package features.cases;

import org.noear.folkmq.FolkMQ;
import org.noear.folkmq.client.*;
import org.noear.folkmq.borker.MqBorkerDefault;
import org.noear.socketd.transport.core.Reply;
import org.noear.socketd.transport.core.entity.StringEntity;

/**
 * @author noear
 * @since 1.2
 */
public class TestCase25_rpc extends BaseTestCase {
    public TestCase25_rpc(int port) {
        super(port);
    }

    @Override
    public void start() throws Exception {
        super.start();

        //服务端
        server = new MqBorkerDefault()
                .start(getPort());

        //客户端
        client = FolkMQ.createClient("folkmq://127.0.0.1:" + getPort())
                .nameAs("app1")
                .connect();

        client.listen(new MqRouter(m -> m.getTag()).doOnConsume(m -> {

        }).doOn("test.hello", m -> {
            m.response(new StringEntity(m.getSender() + ": me to! rev: " + m.getBodyAsString()));
        }));


        MqClient client2 = FolkMQ.createClient("folkmq://127.0.0.1:" + getPort())
                .nameAs("app2")
                .connect();


        //开始 rpc 请求
        Reply reply = client2.send(new MqMessage("hello").tag("test.hello"), client.name()).await();
        String rst = reply.dataAsString();

        //检验客户端
        assert rst.contains("hello");
        assert rst.contains("me to");
        assert rst.contains(client2.name());
    }
}
