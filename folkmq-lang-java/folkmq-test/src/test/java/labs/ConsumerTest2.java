package labs;

import org.noear.folkmq.FolkMQ;
import org.noear.folkmq.client.MqClient;

public class ConsumerTest2 {
    public static void main(String[] args) throws Exception {

        //客户端
        MqClient client = FolkMQ.createClient("folkmq://127.0.0.1:18602?ak=folkmq&sk=YapLHTx19RlsEE16")
                .connect();

        client.subscribe("test", "b", message -> {
            //System.out.println("::" + topic + " - " + message);
        });
    }
}
