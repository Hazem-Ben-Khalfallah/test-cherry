package com.blacknebula.testcherry.refactor.listener;

import com.blacknebula.testcherry.TestFrameworkNotConfigured;
import com.blacknebula.testcherry.model.BDDCore;
import com.blacknebula.testcherry.model.TestClass;
import com.blacknebula.testcherry.model.TestMethod;
import com.blacknebula.testcherry.util.BddUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.refactoring.listeners.RefactoringElementListener;
import com.intellij.refactoring.listeners.RefactoringElementListenerProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * Creado por: jaime
 * 7/10/11
 */
public class TestMethodRefactoringElementListenerProvider implements RefactoringElementListenerProvider {


    @Override
    public RefactoringElementListener getListener(final PsiElement element) {
        if (element instanceof PsiMethod) {


            final ArrayList<TestMethod> testMethodsToRefactor = new ArrayList<TestMethod>();
            final String oldSutMethodName = ((PsiMethod) element).getName();
            final TestClass testClass;


            try {
                testClass = BDDCore.createTestClass(BddUtil.getParentEligibleForTestingPsiClass(element));
                List<TestMethod> allMethods = testClass.getAllMethods();
                for (TestMethod allMethod : allMethods) {
                    if (allMethod.getSutMethod().equals(element) && allMethod.reallyExists()) {
                        testMethodsToRefactor.add(allMethod);
                    }
                }
            } catch (TestFrameworkNotConfigured testFrameworkNotConfigured) {

                // TODO log it
                return null;
            }

            // verify it is a test method
            return new MyRefactoringElementListener(testMethodsToRefactor, testClass, oldSutMethodName);

        } else {

            return null;
        }

    }

}
