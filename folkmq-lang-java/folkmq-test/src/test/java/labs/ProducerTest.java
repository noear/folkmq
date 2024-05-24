package labs;

import org.noear.folkmq.FolkMQ;
import org.noear.folkmq.client.MqClient;
import org.noear.folkmq.client.MqMessage;

public class ProducerTest {
    public static void main(String[] args) throws Exception {
        //客户端
        MqClient client = FolkMQ.createClient("folkmq://127.0.0.1:18602?ak=folkmq&sk=YapLHTx19RlsEE16")
                .connect();

        long i = 0;
        while (true) {
            client.publishAsync("/jlwu/receive/gateway", new MqMessage("hot-" + i));
            i++;
        }
    }
}
