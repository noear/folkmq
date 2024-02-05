package features;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.noear.folkmq.client.MqClient;
import org.noear.folkmq.client.MqClientDefault;
import org.noear.folkmq.common.MqApis;
import org.noear.snack.ONode;

import java.io.IOException;

/**
 * @author noear
 * @since 1.0
 */
public class CallTest {
    private static MqClient client;
    private static String apiToken = "GhVPG@hjJMViC7xN";

    @BeforeAll
    public static void init() throws IOException {
        client = new MqClientDefault("folkmq://127.0.0.1:18602")
                .config(c -> c.metaPut("ak", "").metaPut("sk", ""))
                .connect();
    }

    @Test
    public void MQ_QUEUE_LIST() throws Exception {
        String json = client.call(MqApis.MQ_QUEUE_LIST, apiToken, "test", "a").get();
        System.out.println(json);
        ONode oNode = ONode.loadStr(json);
        assert oNode.get("code").getInt() == 200;
        assert oNode.get("data").isArray();
    }

    @Test
    public void MQ_QUEUE_VIEW_MESSAGE() throws Exception {
        String json = client.call(MqApis.MQ_QUEUE_VIEW_MESSAGE, apiToken, "test", "a").get();
        System.out.println(json);
        ONode oNode = ONode.loadStr(json);

        if (oNode.get("code").getInt() == 200) {
            assert oNode.get("data").isObject();
        } else {
            assert oNode.get("code").getInt() == 400;
        }
    }

    @Test
    public void MQ_QUEUE_VIEW_SESSION() throws Exception {
        String json = client.call(MqApis.MQ_QUEUE_VIEW_SESSION, apiToken, "test", "a").get();
        System.out.println(json);
        ONode oNode = ONode.loadStr(json);
        assert oNode.get("code").getInt() == 200;
        assert oNode.get("data").isArray();
    }

    @Test
    public void MQ_QUEUE_FORCE_CLEAR() throws Exception {
        String json = client.call(MqApis.MQ_QUEUE_FORCE_CLEAR, apiToken, "test", "a").get();
        System.out.println(json);
        ONode oNode = ONode.loadStr(json);
        assert oNode.get("code").getInt() == 200;
    }

    @Test
    public void MQ_QUEUE_FORCE_DELETE() throws Exception {
        String json = client.call(MqApis.MQ_QUEUE_FORCE_DELETE, apiToken, "test", "a").get();
        System.out.println(json);
        ONode oNode = ONode.loadStr(json);
        assert oNode.get("code").getInt() == 200;
    }

    @Test
    public void MQ_QUEUE_FORCE_DISTRIBUTE() throws Exception {
        String json = client.call(MqApis.MQ_QUEUE_FORCE_DISTRIBUTE, apiToken, "test", "a").get();
        System.out.println(json);
        ONode oNode = ONode.loadStr(json);
        assert oNode.get("code").getInt() == 200;
    }
}
