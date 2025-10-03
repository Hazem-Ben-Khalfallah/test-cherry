package com.blacknebula.testcherry.testframework;

import com.blacknebula.testcherry.util.BddUtil;
import com.blacknebula.testcherry.util.PostponedOperations;
import com.blacknebula.testcherry.util.CommentStringEscaper;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiNameValuePair;
import com.intellij.testIntegration.TestFramework;
import org.jetbrains.annotations.NotNull;

public class JUnit5Strategy extends JUnitStrategyBase {

    private final boolean useDescriptiveName;

    public JUnit5Strategy(Project project, NamingConvention namingConvention, boolean useDescriptiveName) {
        super(project, namingConvention);
        this.useDescriptiveName = useDescriptiveName;
    }

    @Override
    public TestFramework getTestFramework() {
        return BddUtil.findTestFrameworkByName("JUnit5");
    }

    @Override
    public @NotNull PsiMethod createBackingTestMethod(PsiClass testClass, PsiMethod sutMethod, String testDescription) {
        final PsiMethod psiMethod = super.createBackingTestMethod(testClass, sutMethod, testDescription);

        // Conditionally add the @DisplayName annotation based on constructor flag
        if (useDescriptiveName) {
            String displayNameRaw = "should " + (testDescription == null ? "" : testDescription);
            // Build a safe constant expression for Java source (handles escaping and avoids raw "\\uXXXX" hazard)
            String valueExpr = CommentStringEscaper.toJavaStringConstantExpression(displayNameRaw);
            String annotationText = String.format("@DisplayName(%s)", valueExpr);
            PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(psiMethod.getProject());
            PsiAnnotation dummyAnnotation = elementFactory.createAnnotationFromText(annotationText, psiMethod);
            PsiNameValuePair[] nameValuePairs = dummyAnnotation.getParameterList().getAttributes();

            AddAnnotationFix displayNameFix = new AddAnnotationFix("org.junit.jupiter.api.DisplayName", psiMethod, nameValuePairs);
            if (displayNameFix.isAvailable(psiMethod.getProject(), psiMethod.getContainingFile())) {
                PostponedOperations.performLater(psiMethod.getProject(), psiMethod.getContainingFile(), displayNameFix::invoke);
            }
        }

        //  add the @Test annotation to the method.
        AddAnnotationFix fix = new AddAnnotationFix("org.junit.jupiter.api.Test", psiMethod);
        if (fix.isAvailable(psiMethod.getProject(), psiMethod.getContainingFile())) {
            PostponedOperations.performLater(psiMethod.getProject(), psiMethod.getContainingFile(), fix::invoke);
        }

        return psiMethod;
    }

    @Override
    protected String getAssertionClassSimpleName() {
        return "Assertions";
    }

    @Override
    protected String getFrameworkBasePackage() {
        return "org.junit.jupiter.api";
    }

}
