package com.blacknebula.testcherry.util;

import org.jetbrains.annotations.NonNls;

/**
 * Created by IntelliJ IDEA.
 * User: JHABLUTZEL
 * Date: Dec 23, 2010
 * Time: 10:42:50 AM
 * To change this template use File | Settings | File Templates.
 */
public final class Constants {

    @NonNls
    public static final String DEF_TEST_FRAMEWORK = "JUNIT3";
    public static final String BDD_TAG = "should";
    public static final String VERIFIES_DOC_TAG = "verifies";

    @NonNls
    public static final String FRAMEWORK_PLACEHOLDER = "-";

    private Constants() {
        throw new AssertionError();
    }

}
