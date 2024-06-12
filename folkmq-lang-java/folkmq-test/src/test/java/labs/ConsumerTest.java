package labs;

import org.noear.folkmq.FolkMQ;
import org.noear.folkmq.client.MqClient;
import org.noear.socketd.transport.core.traffic.TrafficLimiterDefault;

public class ConsumerTest {
    public static void main(String[] args) throws Exception {
        //客户端
        MqClient client = FolkMQ.createClient("folkmq://127.0.0.1:18602?ak=folkmq&sk=YapLHTx19RlsEE16")
                .nameAs("iot")
                .config(c -> c.trafficLimiter(new TrafficLimiterDefault(10_000)))
                .connect();

        //订阅
        client.subscribe("/jlwu/receive/gateway",  message -> {
            //System.out.println("::" + message.getTopic() + " - " + message);
        });
    }
}
