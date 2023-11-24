package demo;

import org.noear.folkmq.client.MqClient;
import org.noear.folkmq.client.MqClientImpl;
import org.noear.folkmq.client.MqSubscription;

/**
 * @author noear
 * @since 1.0
 */
public class ClientDemo2 {
    public static void main(String[] args) throws Exception {

        //客户端
        MqClient client = new MqClientImpl(
                "folkmq://127.0.0.1:9393?accessKey=folkmq&accessSecretKey=YapLHTx19RlsEE16")
                .autoAck(false);

        //订阅
        //订阅
        client.subscribe("demo", new MqSubscription("b",  ((topic, message) -> {
            if(message.getTimes() < 2){
                System.out.println("ClientDemo2-no::" + topic + " - " + message);
                message.acknowledge(false);
            }else{
                System.out.println("ClientDemo2-ok::" + topic + " - " + message);
                message.acknowledge(true);
            }
        })));
    }
}
