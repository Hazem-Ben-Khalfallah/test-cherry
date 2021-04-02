package com.blacknebula.testcherry.testframework;

import com.blacknebula.testcherry.util.BddUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.testIntegration.TestFramework;
import org.jetbrains.annotations.NotNull;

/**
 * User: JHABLUTZEL
 * Date: 09/11/2010
 * Time: 03:06:09 PM
 */
public class JUnit4Strategy extends JUnitStrategyBase {


    public JUnit4Strategy(Project project, NamingConvention namingConvention) {
        super(project, namingConvention);
    }


    @Override
    public TestFramework getTestFramework() {
        return BddUtil.findTestFrameworkByName("JUnit4");
    }

    @Override
    public @NotNull PsiMethod createBackingTestMethod(PsiClass testClass, PsiMethod sutMethod, String testDescription) {
        final PsiMethod psiMethod = super.createBackingTestMethod(testClass, sutMethod, testDescription);
        //  add the annotation to the method
        AddAnnotationFix fix = new AddAnnotationFix("org.junit.Test", psiMethod);
        if (fix.isAvailable(sutMethod.getProject(), psiMethod.getContainingFile())) {
            fix.invoke();
        }

        return psiMethod;
    }

    @Override
    protected String getFrameworkBasePackage() {
        return "org.junit";
    }

    @Override
    protected String getAssertionClassSimpleName() {
        return "Assert";
    }

}
