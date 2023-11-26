package features;

import features.cases.*;
import org.junit.jupiter.api.Test;

/**
 * @author noear
 * @since 1.0
 */
public class CaseTest {
    @Test
    public void TestCase01_send() throws Exception {
        BaseTestCase testCase = new TestCase01_send(2001);
        testCase.start();
        testCase.stop();
    }

    @Test
    public void TestCase02_scheduled() throws Exception {
        BaseTestCase testCase = new TestCase02_scheduled(2002);
        testCase.start();
        testCase.stop();
    }

    @Test
    public void TestCase03_ack_retry() throws Exception {
        BaseTestCase testCase = new TestCase03_ack_retry(2003);
        testCase.start();
        testCase.stop();
    }

    @Test
    public void TestCase04_qos0() throws Exception {
        BaseTestCase testCase = new TestCase04_qos0(2004);
        testCase.start();
        testCase.stop();
    }
}
