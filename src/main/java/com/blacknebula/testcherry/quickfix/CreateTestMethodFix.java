package com.blacknebula.testcherry.quickfix;

import com.blacknebula.testcherry.TestCherryBundle;
import com.blacknebula.testcherry.model.TestClass;
import com.blacknebula.testcherry.model.TestMethod;
import com.blacknebula.testcherry.model.TestMethodImpl;
import com.blacknebula.testcherry.testframework.TestFrameworkStrategy;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

/**
 * User: jhe
 */
public class CreateTestMethodFix implements IntentionAction {

    private final TestMethod testMethod;

    public CreateTestMethodFix(TestMethod testMethod) {
        this.testMethod = testMethod;
    }

    public TestMethod getTestMethod() {
        return testMethod;
    }

    /**
     * @return
     * @should assert the name for the intention has the fqn for the test method
     */
    @NotNull
    @Override
    public String getText() {

        TestMethodImpl tMethod = (TestMethodImpl) testMethod;
        TestFrameworkStrategy testFrameworkStrategy = tMethod.getTestFrameworkStrategy();
        String testMethodName = testFrameworkStrategy.getExpectedNameForThisTestMethod(testMethod.getSutMethod().getName(), testMethod.getDescription());
        TestClass parent = tMethod.getParent();
        String candidateClassName = testFrameworkStrategy.getCandidateTestClassName(parent.getClassUnderTest());
        return TestCherryBundle.message("plugin.testCherry.createtestmethod",
                candidateClassName + "." + testMethodName + "()");
    }

    @NotNull
    @Override
    public String getFamilyName() {
        return TestCherryBundle.message("plugin.testCherry.bdd.family");
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
        // TODO check better under what conditions it should be unavailable
        // com.intellij.codeInsight.daemon.model.quickfix.SimplifyBooleanExpressionFix.isAvailable()
        return true;
    }


    /**
     * @throws IncorrectOperationException
     * @should jump to target test method in editor
     * @should create test method
     */
    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
        //  create test method
        invoke();
    }

    /**
     * It will provide undo capability
     *
     * @see IntentionAction#startInWriteAction()
     */
    @Override
    public boolean startInWriteAction() {

        return true;
    }

    public void invoke() {
        testMethod.create();
        testMethod.navigate();
    }


}
