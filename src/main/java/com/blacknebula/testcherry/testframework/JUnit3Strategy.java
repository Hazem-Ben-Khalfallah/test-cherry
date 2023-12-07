package com.blacknebula.testcherry.testframework;

import com.blacknebula.testcherry.util.BddUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiJavaCodeReferenceElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.testIntegration.TestFramework;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

/**
 * User: JHABLUTZEL
 * Date: 09/11/2010
 * Time: 03:06:09 PM
 * <p/>
 * import junit.framework.Test and Assert in the same package
 * make class to extends junit.framework.TestCase
 */
public class JUnit3Strategy extends JUnitStrategyBase {

    public JUnit3Strategy(Project project, NamingConvention namingConvention) {
        super(project, namingConvention);
    }

    @NotNull
    @Override
    public String getExpectedNameForThisTestMethod(String sutMethodName, String description) {
        String s = super.getExpectedNameForThisTestMethod(sutMethodName, description);
        return "test" + StringUtils.capitalize(s);
    }

    @Override
    public TestFramework getTestFramework() {
        return BddUtil.findTestFrameworkByName("JUnit3");
    }

    /**
     * @param testClass
     * @param sutMethod
     * @param testDescription @return
     * @return
     * @should add junit 3 specific imports
     */
    @Override // just created to test implementation details for this specific framework
    public @NotNull PsiMethod createBackingTestMethod(PsiClass testClass, PsiMethod sutMethod, String testDescription) {
        return super.createBackingTestMethod(testClass, sutMethod, testDescription);
    }

    /**
     * @param sutClass
     * @param sourceRoot
     * @return
     * @should create a test class that extends TestCase
     */
    @Override  // overridden to write test method only
    public PsiClass createBackingTestClass(PsiClass sutClass, PsiDirectory sourceRoot) {
        return super.createBackingTestClass(sutClass, sourceRoot);
    }

    @Override
    protected String getFrameworkBasePackage() {
        String s = "junit.framework";
        return s;
    }

    @Override
    protected String getAssertionClassSimpleName() {
        return "Assert";
    }

    @Override
    protected void afterCreatingClass(Project project, PsiClass backingTestClass) {
        PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(project);
        PsiJavaCodeReferenceElement referenceElementByFQClassName = elementFactory.createReferenceElementByFQClassName("junit.framework.TestCase", GlobalSearchScope.allScope(project));
        backingTestClass.getExtendsList().add(referenceElementByFQClassName);
    }
}
