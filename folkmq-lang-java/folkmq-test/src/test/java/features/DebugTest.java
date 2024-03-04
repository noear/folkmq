package features;

import features.cases.BaseTestCase;
import features.cases.TestCase19_sequence;
import features.cases.TestCase21_tran_commit;

public class DebugTest {
    public static void main(String[] args) throws Exception{
        BaseTestCase testCase = new TestCase19_sequence(18602);
        testCase.start();
    }
}
