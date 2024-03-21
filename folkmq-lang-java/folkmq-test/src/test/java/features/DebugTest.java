package features;

import features.cases.BaseTestCase;
import features.cases.TestCase19_sequence;
import features.cases.TestCase21_tran_commit;
import features.cases.TestCase28_packaging_loop;

public class DebugTest {
    public static void main(String[] args) throws Exception{
        BaseTestCase testCase = new TestCase28_packaging_loop(18602);
        testCase.start();
    }
}
