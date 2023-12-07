package com.blacknebula.testcherry.inspection;

import com.blacknebula.testcherry.model.BDDCore;
import com.blacknebula.testcherry.model.TestCherrySettings;
import com.blacknebula.testcherry.model.TestClass;
import com.blacknebula.testcherry.model.TestMethod;
import com.blacknebula.testcherry.model.TestMethodImpl;
import com.blacknebula.testcherry.quickfix.CreateTestMethodFix;
import com.blacknebula.testcherry.testframework.NamingConvention;
import com.blacknebula.testcherry.testframework.SupportedFrameworks;
import com.blacknebula.testcherry.testframework.TestFrameworkStrategy;
import com.blacknebula.testcherry.util.BddUtil;
import com.blacknebula.testcherry.util.Constants;
import com.intellij.codeInsight.daemon.HighlightDisplayKey;
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.CustomSuppressableInspectionTool;
import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.SuppressIntentionAction;
import com.intellij.codeInspection.SuppressManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiAnonymousClass;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.jsp.jspJava.JspClass;
import com.intellij.psi.javadoc.PsiDocTag;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * This inspection will search for classes not tested yet, and it will inspect should annotations
 * without a test method created
 */
public class MissingTestMethodInspection extends AbstractBaseJavaLocalInspectionTool implements CustomSuppressableInspectionTool {

    private static final Logger LOG = Logger.getInstance(MissingTestMethodInspection.class);

    @Nls
    @NotNull
    @Override
    public String getGroupDisplayName() {
        return "BDD";
    }

    @Override
    public boolean isEnabledByDefault() {
        return true;
    }

    @Nls
    @NotNull
    @Override
    public String getDisplayName() {
        return "Unused Should Annotations";
    }

    @NotNull
    @Override
    public String getShortName() {
        return "UnusedShould";
    }


    /**
     * @should create problem for classes without backing class
     * @should create problem for should annotations without test methods
     * @should ignore unsupported psiClasses
     */
    @Override
    public ProblemDescriptor[] checkClass(@NotNull PsiClass aClass, @NotNull InspectionManager manager, boolean isOnTheFly) {

        // ignoring unsupported classes
        if (aClass instanceof PsiAnonymousClass || aClass instanceof JspClass) {
            return null;
        }

        if (aClass.getQualifiedName() == null) { // TODO research the API for the best way to make this check
            // aClass.getQualifiedName() is returning null just after writting an anonymous class, it is somehow related to the inner stub
            // see https://github.com/skarootz/GenerateTestCases/issues/27

            return ProblemDescriptor.EMPTY_ARRAY;
        }

        Project project = aClass.getProject();
        TestCherrySettings testCherrySettings = TestCherrySettings.getInstance(project);

        String testFramework;
        if (!ApplicationManager.getApplication().isUnitTestMode()) {
            testFramework = testCherrySettings.getTestFramework();
            if (StringUtils.isEmpty(testFramework)) {
                return null;
            }
        } else {
            testFramework = Constants.DEF_TEST_FRAMEWORK;
        }

        //  create TestClass for current class
        NamingConvention namingConvention = testCherrySettings.getNamingConvention();
        TestFrameworkStrategy strategyForFramework = SupportedFrameworks.getStrategyForFramework(project, testFramework, namingConvention);
        TestClass testClass = BDDCore.createTestClass(aClass, strategyForFramework);


        //  highlight warning should cover test class name
        //  if test class doesn't exists place warning at class level
        if (!testClass.reallyExists()) {
            //  create warning
            if (testClass.getClassUnderTest() != null && testClass.getClassUnderTest().getNameIdentifier() != null) {
                return new ProblemDescriptor[]{
                        manager.createProblemDescriptor(testClass.getClassUnderTest().getNameIdentifier(), "Missing Test Class",
                                isOnTheFly, LocalQuickFix.EMPTY_ARRAY, ProblemHighlightType.GENERIC_ERROR_OR_WARNING)};
            }
        }

        List<ProblemDescriptor> result = new ArrayList<ProblemDescriptor>();

        //  if test class exists place warning at javadoc tags level
        List<TestMethod> methods = testClass.getAllMethods();
        for (TestMethod method : methods) {
            if (!method.reallyExists()) {
                highlightShouldTags(manager, isOnTheFly, result, method);
            }
        }

        // TODO create fix for this problem
        return result.toArray(new ProblemDescriptor[result.size()]);
    }

    @Override
    public SuppressIntentionAction @NotNull [] getSuppressActions(PsiElement element) {
        String shortName = getShortName();
        HighlightDisplayKey key = HighlightDisplayKey.find(shortName);
        if (key == null) {
            throw new AssertionError("HighlightDisplayKey.find(" + shortName + ") is null. Inspection: " + getClass());
        }
        return SuppressManager.getInstance().createSuppressActions(key);
    }

    private void highlightShouldTags(InspectionManager manager, boolean isOnTheFly, List<ProblemDescriptor> result, TestMethod method) {
        PsiDocTag backingTag = ((TestMethodImpl) method).getBackingTag();
        List<BddUtil.DocOffsetPair> elementPairsInDocTag = BddUtil.getElementPairsInDocTag(backingTag);
        final CreateTestMethodFix createTestMethodFix = new CreateTestMethodFix(method);

        for (BddUtil.DocOffsetPair docOffsetPair : elementPairsInDocTag) {

            LocalQuickFix localQuickFix = new LocalQuickFix() {
                @NotNull
                public String getName() {
                    return createTestMethodFix.getText();
                }

                public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
                    final PsiElement psiElement = descriptor.getPsiElement();
                    LOG.assertTrue(psiElement.isValid());
                    createTestMethodFix.invoke();
                }

                @NotNull
                public String getFamilyName() {
                    return createTestMethodFix.getFamilyName();
                }
            };

            ProblemDescriptor problemDescriptor = manager.createProblemDescriptor(docOffsetPair.getStart(), docOffsetPair.getEnd(),
                    "Missing test method for should annotation", ProblemHighlightType.GENERIC_ERROR_OR_WARNING, isOnTheFly, localQuickFix == null ? null : new LocalQuickFix[]{localQuickFix});
            result.add(problemDescriptor);
        }
    }
}
