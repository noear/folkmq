package labs;

import org.noear.folkmq.FolkMQ;
import org.noear.folkmq.client.MqClient;
import org.noear.folkmq.client.MqMessage;
import org.noear.socketd.transport.core.traffic.TrafficLimiterDefault;

public class ProducerTest {
    public static void main(String[] args) throws Exception {
        //客户端
        MqClient client = FolkMQ.createClient("folkmq://127.0.0.1:18602?ak=folkmq&sk=YapLHTx19RlsEE16")
                .config(c -> c.trafficLimiter(new TrafficLimiterDefault(10_000)))
                .connect();

        StringBuilder buf = new StringBuilder();
        while (buf.length() < 1024) {
            buf.append("0123456789abcdef;");
        }

        long i = 0;
        while (true) {
            client.publishAsync("/jlwu/receive/gateway", new MqMessage(buf.toString() + i++));
        }
    }
}
