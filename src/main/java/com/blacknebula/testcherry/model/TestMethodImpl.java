package com.blacknebula.testcherry.model;

import com.blacknebula.testcherry.testframework.TestFrameworkStrategy;
import com.blacknebula.testcherry.util.BddUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.javadoc.PsiDocTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.concurrency.Promise;

/**
 * User: Jaime Hablutzel
 */
public class TestMethodImpl implements TestMethod {


    private final TestFrameworkStrategy testFrameworkStrategy;
    private final PsiDocTag shouldTag;
    private final String description;
    private final TestClass parent;

    // TODO create a strategy for creating test methods
    private final Project project;
    private PsiMethod sutMethod;

    // package protected
    private TestMethodImpl(@NotNull PsiDocTag shouldTag, @NotNull TestClass parent, TestFrameworkStrategy frameworkStrategy) {

        // TODO instantiate an strategy
        testFrameworkStrategy = frameworkStrategy;

        this.shouldTag = shouldTag;
        this.project = shouldTag.getProject();


        //  obtener el metodo a partir del docTag
        resolveSutMethod(shouldTag);
        //  initialize the description
        this.description = BddUtil.getShouldTagDescription(shouldTag);

        //  bind the current test parent...
        // TODO get this using the shouldTag, or investigate it better
        // TO get the TestClass parent from here without passing it through the constructor
        // it would be needed to implement a registry where we could look for instances for
        // some determined class to guarantee that uniqueness of parents for test methods
        //this.parent = ((PsiMethod)shouldTag.getParent().getParent()).getContainingClass();

        // FIXME parent is being used to get the backing class, maybe delete this dependency??

        this.parent = parent;
    }

    /**
     * Static factory method
     * Effective Java item 1
     *
     * @param shouldTag
     * @param parent
     * @param frameworkStrategy
     * @return
     */
    static TestMethodImpl newInstance(@NotNull PsiDocTag shouldTag, @NotNull TestClass parent, TestFrameworkStrategy frameworkStrategy) {
        return new TestMethodImpl(shouldTag, parent, frameworkStrategy);
    }

    @Override
    public TestFrameworkStrategy getTestFrameworkStrategy() {
        return testFrameworkStrategy;
    }

    public TestClass getParent() {
        return parent;
    }

    private void resolveSutMethod(PsiDocTag shouldTag) {
        PsiMethod method = (PsiMethod) shouldTag.getParent().getContext();
        this.sutMethod = method;
    }

    @Override
    public boolean reallyExists() {
        PsiMethod method1 = null;
        if (this.parent.getBackingElement() != null) {
            method1 = testFrameworkStrategy.findBackingTestMethod(this.parent.getBackingElement(), sutMethod, description);
        }
        PsiMethod method = method1;

        return null != method;

    }

    @Override
    public void navigate() {
        this.getBackingElement().navigate(true);
    }

    @Override
    public void create() {
        if (parent == null) {
            // TODO need to look for the parent psi test class in some other way
            // TODO create a stub for the parent or look in registry
            // TODO log it
        } else if (!parent.reallyExists()) {
            //  if parent doesn't exist, create it
            parent.create(null);
        }


        PsiMethod realTestMethod = testFrameworkStrategy.createBackingTestMethod(parent.getBackingElement(), sutMethod, description);

        CodeStyleManager codeStyleManager = CodeStyleManager.getInstance(project);

        codeStyleManager.reformat(realTestMethod); // to reformat javadoc

    }

    public String getDescription() {
        return description;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public PsiMethod getSutMethod() {
        return this.sutMethod;
    }

    public PsiDocTag getBackingTag() {
        return shouldTag;
    }

    public PsiMethod getBackingElement() {
        PsiMethod method = null;
        if (this.parent.getBackingElement() != null) {
            method = testFrameworkStrategy.findBackingTestMethod(this.parent.getBackingElement(), sutMethod, description);
        }
        return method;
    }
}
