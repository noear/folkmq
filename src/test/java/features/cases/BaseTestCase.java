package features.cases;

/**
 * @author noear
 * @since 1.0
 */
public abstract class BaseTestCase {
    private final int port;

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
        System.out.println("------------------ (test stop: port=" + getPort() + ")------------------");
    }
}
