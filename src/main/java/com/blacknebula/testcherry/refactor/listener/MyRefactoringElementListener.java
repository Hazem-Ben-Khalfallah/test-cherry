package com.blacknebula.testcherry.refactor.listener;

import com.blacknebula.testcherry.model.TestClass;
import com.blacknebula.testcherry.model.TestMethod;
import com.blacknebula.testcherry.testframework.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.refactoring.listeners.RefactoringElementListener;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * // TODO move this logic to TestMethod
 * Creado por: jaime
 * 7/10/11
 */
class MyRefactoringElementListener implements RefactoringElementListener {

    private final ArrayList<TestMethod> methodsToRefactor;
    private final TestClass testClass;
    private final String oldMethodName;

    public MyRefactoringElementListener(ArrayList<TestMethod> methodsToRefactor, TestClass testClass, String oldMethodName) {
        this.methodsToRefactor = methodsToRefactor;
        this.testClass = testClass;
        this.oldMethodName = oldMethodName;
    }

    @Override
    public void elementMoved(@NotNull PsiElement newElement) {

        // TODO get test framework

        // TODO get target class

        //BddUtil.getParentEligibleForTestingPsiClass(newElement) new target
        // do nothing
    }

    /**
     * @param newElement
     */
    @Override
    public void elementRenamed(@NotNull final PsiElement newElement) {
        for (final TestMethod testMethod : methodsToRefactor) {

            //  update method name
            final TestFrameworkStrategy testFrameworkStrategy = testClass.getTestFrameworkStrategy();

            // get method with name expectedNameForThisTestMethod and rename it directly
            PsiClass psiTestClass = testClass.getBackingElement();
            PsiMethod[] methodsByName = psiTestClass.findMethodsByName(testFrameworkStrategy.getExpectedNameForThisTestMethod(oldMethodName, testMethod.getDescription()), false);

            for (final PsiMethod psiMethod : methodsByName) {
                ApplicationManager.getApplication().runWriteAction(new Runnable() {
                    public void run() {
                        psiMethod.setName(testFrameworkStrategy.getExpectedNameForThisTestMethod(((PsiMethod) newElement).getName(), testMethod.getDescription()));
                    }
                });
            }


        }
    }
}
