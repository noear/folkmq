package features.cases;

import org.noear.folkmq.client.MqClient;
import org.noear.folkmq.borker.MqBorker;

/**
 * @author noear
 * @since 1.0
 */
public abstract class BaseTestCase {
    private final int port;
    protected MqBorker server;
    protected MqClient client;

    public int getPort() {
        return port;
    }

    public BaseTestCase(int port) {
        this.port = port;
    }

    /**
     * 开始测试
     */
    public void start() throws Exception {
        System.out.println("------------------ (test start: port=" + getPort() + ")------------------");
    }

    /**
     * 停止测试
     */
    public void stop() throws Exception {
        if (server != null) {
            server.stop();
        }

        if (client != null) {
            client.disconnect();
        }

        System.out.println("------------------ (test stop: port=" + getPort() + ")------------------");
    }
}
