package com.blacknebula.testcherry.testframework;

import com.blacknebula.testcherry.util.BddUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiStatement;
import com.intellij.testIntegration.TestFramework;
import org.jetbrains.annotations.NotNull;

/**
 * Creado por: jaime
 * 10/3/12
 */
public class TestNGStrategy extends AbstractTestFrameworkStrategy {


    public TestNGStrategy(Project project, NamingConvention namingConvention) {
        super(project, namingConvention);
    }

    @Override
    public TestFramework getTestFramework() {
        return BddUtil.findTestFrameworkByName("TestNG");
    }


    /**
     * @should add org.testng.Assert import if it doesn't collide with another x.Assert import
     * @should add org.testng.Assert.fail("Not yet implemented"); statement to method under test
     */
    @Override
    public @NotNull PsiMethod createBackingTestMethod(@NotNull PsiClass testClass, @NotNull PsiMethod sutMethod, @NotNull String testDescription) {
        final PsiMethod backingTestMethod = super.createBackingTestMethod(testClass, sutMethod, testDescription);
        // TODO add import for org.testng.Assert if it doesn't collides with another x.
        //  Assert import as in com.blacknebula.javatestgenerator.testframework.JUnitStrategyBase.createBackingTestMethod()

        PsiJavaFile javaFile = (PsiJavaFile) testClass.getContainingFile();
        boolean assertImportExists = javaFile.getImportList().findSingleImportStatement("Assert") != null;

        if (!assertImportExists) {
            BddUtil.addImportToClass(sutMethod.getProject(), testClass, "org.testng.Assert");
        }

        // TODO check if this is org.testng.Assert, if it is: place unqualified statement, else qualify it
        PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(sutMethod.getProject());
        PsiStatement statement;
        if (javaFile.getImportList().findSingleClassImportStatement("org.testng.Assert") != null) {
            statement = elementFactory.createStatementFromText("Assert.fail(\"Not yet implemented\");", null);
        } else {
            statement = elementFactory.createStatementFromText("org.testng.Assert.fail(\"Not yet implemented\");", null);
        }

        //  add failed assert in testng terms

        backingTestMethod.getBody().addAfter(statement, backingTestMethod.getBody().getLastBodyElement());

        //  add the annotation to the method
        final AddAnnotationFix fix = new AddAnnotationFix("org.testng.annotations.Test", backingTestMethod);
        if (fix.isAvailable(sutMethod.getProject(), backingTestMethod.getContainingFile())) {
            fix.invoke();
        }
        return backingTestMethod;
    }


}
