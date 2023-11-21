package benchmark;

import org.noear.folkmq.client.MqClient;
import org.noear.folkmq.client.MqClientImpl;
import org.noear.folkmq.client.Subscription;
import org.noear.folkmq.server.MqServer;
import org.noear.folkmq.server.MqServerImpl;

/**
 * @author noear 2023/11/21 created
 */
public class BenchmarkTest {
    public static void main(String[] args) throws Exception {
        //服务端
        MqServer server = new MqServerImpl()
                .addAccess("folkmq", "YapLHTx19RlsEE16")
                .start(9393);

        Thread.sleep(1000);

        //客户端
        MqClient client = new MqClientImpl(
                "folkmq://127.0.0.1:9393?accessKey=folkmq&accessSecretKey=YapLHTx19RlsEE16");

        //订阅
        client.subscribe("demo", new Subscription("a", ((topic, message) -> {
            //System.out.println("ClientDemo1::" + topic + " - " + message);
        })));

        //预热
        for (int i = 0; i < 10000; i++) {
            client.publish("demo", "hi-" + i);
        }

        long start_time = System.currentTimeMillis();
        //发布
        for (int i = 0; i < 100_0000; i++) {
            client.publish("demo", "hi-" + i);
        }
        long time_span = System.currentTimeMillis() - start_time;

        System.out.println(time_span);
    }
}
