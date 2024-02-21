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
        BaseTestCase testCase = new TestCase01_publish(2001);
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

    @Test
    public void TestCase08_expiration() throws Exception {
        BaseTestCase testCase = new TestCase08_expiration(2008);
        testCase.start();
        testCase.stop();
    }

    @Test
    public void TestCase09_persistent() throws Exception {
        BaseTestCase testCase = new TestCase09_persistent(2009);
        testCase.start();
        testCase.stop();
    }

    ///

    @Test
    public void TestCase11_send_n() throws Exception {
        BaseTestCase testCase = new TestCase11_send_n(2011);
        testCase.start();
        testCase.stop();
    }

    @Test
    public void TestCase12_scheduled_n() throws Exception {
        BaseTestCase testCase = new TestCase12_scheduled_n(2012);
        testCase.start();
        testCase.stop();
    }

    @Test
    public void TestCase13_ack_retry_n() throws Exception {
        BaseTestCase testCase = new TestCase13_ack_retry_n(2013);
        testCase.start();
        testCase.stop();
    }

    @Test
    public void TestCase14_qos0_n() throws Exception {
        BaseTestCase testCase = new TestCase14_qos0_n(2014);
        testCase.start();
        testCase.stop();
    }

    @Test
    public void TestCase15_send_multi_server() throws Exception {
        BaseTestCase testCase = new TestCase15_send_multi_server(2015);
        testCase.start();
        testCase.stop();
    }

    @Test
    public void TestCase16_ack_retry_multi_server() throws Exception {
        BaseTestCase testCase = new TestCase16_ack_retry_multi_server(2016);
        testCase.start();
        testCase.stop();
    }

    @Test
    public void TestCase17_unpublish() throws Exception {
        BaseTestCase testCase = new TestCase17_unpublish(2017);
        testCase.start();
        testCase.stop();
    }

    @Test
    public void TestCase18_batch_subscribe() throws Exception {
        BaseTestCase testCase = new TestCase18_batch_subscribe(2018);
        testCase.start();
        testCase.stop();
    }

    @Test
    public void TestCase19_sequence() throws Exception {
        BaseTestCase testCase = new TestCase19_sequence(2019);
        testCase.start();
        testCase.stop();
    }


    @Test
    public void TestCase20_sequence_async() throws Exception {
        BaseTestCase testCase = new TestCase20_sequence_async(2020);
        testCase.start();
        testCase.stop();
    }

    @Test
    public void TestCase21_tran_commit() throws Exception {
        BaseTestCase testCase = new TestCase21_tran_commit(2021);
        testCase.start();
        testCase.stop();
    }

    @Test
    public void TestCase22_tran_commit2() throws Exception {
        BaseTestCase testCase = new TestCase22_tran_commit2(2022);
        testCase.start();
        testCase.stop();
    }

    @Test
    public void TestCase23_tran_rollback() throws Exception {
        BaseTestCase testCase = new TestCase23_tran_rollback(2023);
        testCase.start();
        testCase.stop();
    }

    @Test
    public void TestCase24_tran_rollback2() throws Exception {
        BaseTestCase testCase = new TestCase24_tran_rollback2(2024);
        testCase.start();
        testCase.stop();
    }

    @Test
    public void TestCase25_rpc() throws Exception {
        BaseTestCase testCase = new TestCase25_rpc(2025);
        testCase.start();
        testCase.stop();
    }
}
