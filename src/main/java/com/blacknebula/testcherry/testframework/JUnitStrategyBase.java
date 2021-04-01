package com.blacknebula.testcherry.testframework;

import com.blacknebula.testcherry.util.BddUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiImportStatement;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiStatement;
import org.jetbrains.annotations.NotNull;

/**
 * User: Jaime Hablutzel
 */
public abstract class JUnitStrategyBase extends AbstractTestFrameworkStrategy {

//    private

    private final Project project;

    protected JUnitStrategyBase(Project project, NamingConvention namingConvention) {
        super(project, namingConvention);
        this.project = project;
    }

    /**
     * This method completes the test method structure returned by {@link AbstractTestFrameworkStrategy#createBackingTestMethod(com.intellij.psi.PsiClass, com.intellij.psi.PsiMethod, String)} in the way JUNIT 3 and 4 expect.
     *
     * @param testClass
     * @param sutMethod
     * @param testDescription @return
     * @return
     * @should manage appropiately existence of multiple junit Assert's imports across junit versions
     * @should manage appropiately any condition of the backing test class (imports, existing methods, modifiers, etc)
     * @should add Assert.fail("Not yet implemented") statement to method body
     */
    @Override
    public @NotNull PsiMethod createBackingTestMethod(PsiClass testClass, PsiMethod sutMethod, String testDescription) {
        final PsiMethod realTestMethod = super.createBackingTestMethod(testClass, sutMethod, testDescription);
        PsiJavaFile javaFile = (PsiJavaFile) testClass.getContainingFile();

        boolean assertImportExists = javaFile.getImportList().findSingleImportStatement(getAssertionClassSimpleName()) != null;
        boolean makeFullQualified = false;

        final String assertionClassFullQualifiedName = getFrameworkBasePackage() + "." + getAssertionClassSimpleName();
        //  if Assert exists and is different to both of previous, place fully qualified statement
        if (assertImportExists) {
            //  replace it by ((PsiJavaFile) testClass.getContainingFile()).getImportList()
            final PsiImportStatement psiImportStatement = javaFile.getImportList().findSingleClassImportStatement(assertionClassFullQualifiedName);
            if (psiImportStatement == null) {
                // then it is a weird class
                makeFullQualified = true;
            }
        } else {
            //  create basic import
            BddUtil.addImportToClass(sutMethod.getProject(), testClass, assertionClassFullQualifiedName);
        }


        // org.junit.Assert
        PsiElementFactory elementFactory2 = JavaPsiFacade.getElementFactory(sutMethod.getProject());

        PsiStatement statement;
        if (makeFullQualified) {
            statement = elementFactory2.createStatementFromText(assertionClassFullQualifiedName + ".fail(\"Not yet implemented\");", null);
        } else {
            statement = elementFactory2.createStatementFromText(getAssertionClassSimpleName() + ".fail(\"Not yet implemented\");", null);
        }

        realTestMethod.getBody().addAfter(statement, realTestMethod.getBody().getLastBodyElement());

        return realTestMethod;
    }

    protected abstract String getFrameworkBasePackage();

    protected abstract String getAssertionClassSimpleName();


}
