package com.blacknebula.testcherry.testframework;

import com.blacknebula.testcherry.util.BddUtil;
import com.intellij.codeInsight.intention.AddAnnotationFix;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.CommonDataKeys;
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


    public JUnit4Strategy(Project project) {
        super(project);
    }


    @Override
    public TestFramework getTestFramework() {
        return BddUtil.findTestFrameworkByName("JUnit4");
    }

    @NotNull
    @Override
    public PsiMethod createBackingTestMethod(PsiClass testClass, PsiMethod sutMethod, String testDescription) {
        PsiMethod backingTestMethod = super.createBackingTestMethod(testClass, sutMethod, testDescription);
        DataManager.getInstance().getDataContextFromFocusAsync()
                .then(dataContext -> dataContext.getData(CommonDataKeys.EDITOR))
                .onSuccess(editor -> {
                    if (editor != null) {
                        //  add the annotation to the method
                        AddAnnotationFix fix = new AddAnnotationFix("org.junit.Test", backingTestMethod);
                        if (fix.isAvailable(sutMethod.getProject(), editor, backingTestMethod.getContainingFile())) {
                            fix.invoke(sutMethod.getProject(), editor, backingTestMethod.getContainingFile());
                        }
                    }
                });
        return backingTestMethod;
    }

    @Override
    protected String getFrameworkBasePackage() {
        return "org.junit";
    }
}
