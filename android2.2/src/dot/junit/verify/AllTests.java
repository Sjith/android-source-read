/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dot.junit.verify;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * Listing of all the tests that are to be run.
 */
public class AllTests {

    public static void run() {
        TestRunner.main(new String[] {AllTests.class.getName()});
    }

    public static final Test suite() {
        TestSuite suite = new TestSuite("Tests for dalvik vm: test that "
                + "structurally damaged files are rejected by the verifier");
        suite.addTestSuite(dot.junit.verify.a1.Test_a1.class);
        suite.addTestSuite(dot.junit.verify.a3.Test_a3.class);
        suite.addTestSuite(dot.junit.verify.a5.Test_a5.class);
        suite.addTestSuite(dot.junit.verify.b2.Test_b2.class);
        suite.addTestSuite(dot.junit.verify.b3.Test_b3.class);
        suite.addTestSuite(dot.junit.verify.b17.Test_b17.class);

        return suite;
    }
}
