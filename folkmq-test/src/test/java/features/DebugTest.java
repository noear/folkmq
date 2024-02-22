package features;

import features.cases.BaseTestCase;
import features.cases.TestCase21_tran_commit;

public class DebugTest {
    public static void main(String[] args) throws Exception{
        BaseTestCase testCase = new TestCase21_tran_commit(18602);
        testCase.start();
    }
}
