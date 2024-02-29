package demo;

import org.noear.folkmq.FolkMQ;
import org.noear.folkmq.client.MqClient;
import org.noear.folkmq.client.MqMessage;
import org.noear.folkmq.solon.MqSolonListener;

public class DemoApp {
    public static void main(String[] args) throws Exception {
        MqClient client = FolkMQ.createClient("folkmq://127.0.0.1:8602")
                .config(c -> c.metaPut("ak", "").metaPut("sk", ""))
                .nameAs("app2")
                .connect();

        client.listen(new MqSolonListener());


        client.send(new MqMessage("{code:1}").tag("/test/hello").asJson(), "app1").thenReply(resp -> {

        });
    }
}
