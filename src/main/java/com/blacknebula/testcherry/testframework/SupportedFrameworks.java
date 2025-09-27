package com.blacknebula.testcherry.testframework;

import com.blacknebula.testcherry.model.TestCherrySettings;
import com.intellij.openapi.project.Project;

import java.lang.reflect.InvocationTargetException;

/**
 * Enum containing one element per supported framework, every entry should implement {@link TestFrameworkStrategy} and have at least one constructor
 * receiving only one parameter of type {@link Project} even if the implementation doesn't need it FIXME redesign to remove this obligation
 * <p>
 * // TODO create extension point to allow another extensions to create implementations to allow to create test cases
 * <p>
 * User: Jaime Hablutzel
 */
public enum SupportedFrameworks {

    JUNIT3(JUnit3Strategy.class),
    JUNIT4(JUnit4Strategy.class),
    JUNIT5(JUnit5Strategy.class),
    TESTNG(TestNGStrategy.class);

    private final Class<? extends TestFrameworkStrategy> clazz;


    SupportedFrameworks(Class<? extends TestFrameworkStrategy> clazz) {
        this.clazz = clazz;
    }

    /**
     * Should return a framework strategy based on a String
     */
    public static TestFrameworkStrategy getStrategyForFramework(Project project, String testFramework, NamingConvention namingConvention) {

        try {
            SupportedFrameworks supportedFrameworks = SupportedFrameworks.valueOf(testFramework);
            try {
                if (supportedFrameworks == JUNIT5) {
                    boolean useDisplayName = TestCherrySettings.getInstance(project).isUseDescriptiveName();
                    return supportedFrameworks.clazz
                            .getConstructor(Project.class, NamingConvention.class, boolean.class)
                            .newInstance(project, namingConvention, useDisplayName);
                }

                return supportedFrameworks.clazz
                        .getConstructor(Project.class, NamingConvention.class)
                        .newInstance(project, namingConvention);

            } catch (NoSuchMethodException
                     | InvocationTargetException
                     | InstantiationException |
                     IllegalAccessException e) {
                throw new RuntimeException(e);
            }

        } catch (IllegalArgumentException e) {
            throw new UnsupportedOperationException("Unsupported framework: " + testFramework);
        }

    }
}
