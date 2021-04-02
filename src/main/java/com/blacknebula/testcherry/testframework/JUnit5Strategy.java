package com.blacknebula.testcherry.testframework;

import com.blacknebula.testcherry.util.BddUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.testIntegration.TestFramework;
import org.jetbrains.annotations.NotNull;

public class JUnit5Strategy extends JUnitStrategyBase {


    public JUnit5Strategy(Project project, NamingConvention namingConvention) {
        super(project, namingConvention);
    }

    @Override
    public TestFramework getTestFramework() {
        return BddUtil.findTestFrameworkByName("JUnit5");
    }

    @Override
    public @NotNull PsiMethod createBackingTestMethod(PsiClass testClass, PsiMethod sutMethod, String testDescription) {
        final PsiMethod psiMethod = super.createBackingTestMethod(testClass, sutMethod, testDescription);
        //  add the annotation to the method
        AddAnnotationFix fix = new AddAnnotationFix("org.junit.jupiter.api.Test", psiMethod);
        if (fix.isAvailable(sutMethod.getProject(), psiMethod.getContainingFile())) {
            fix.invoke();
        }

        return psiMethod;
    }

    @Override
    protected String getFrameworkBasePackage() {
        return "org.junit.jupiter.api";
    }

    @Override
    protected String getAssertionClassSimpleName() {
        return "Assertions";
    }

}
