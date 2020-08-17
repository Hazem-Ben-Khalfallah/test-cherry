package com.blacknebula.testcherry.refactor;

import com.blacknebula.testcherry.TestFrameworkNotConfigured;
import com.blacknebula.testcherry.model.BDDCore;
import com.blacknebula.testcherry.model.TestClass;
import com.blacknebula.testcherry.model.TestMethod;
import com.blacknebula.testcherry.model.TestMethodImpl;
import com.blacknebula.testcherry.util.BddUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.javadoc.PsiDocTag;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import java.util.List;


public class ShouldReferenceProvider extends PsiReferenceProvider {

    public static final ShouldReferenceProvider INSTANCE = new ShouldReferenceProvider();
    private static final PsiReference[] NO_REFERENCES = new PsiReference[0];
    private static final Logger LOGGER = Logger.getInstance(ShouldReferenceProvider.class);

    private ShouldReferenceProvider() {
    }

    /**
     * Static factory that returns singleton effective java item 3
     *
     * @return
     */
    public static ShouldReferenceProvider getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean acceptsTarget(@NotNull PsiElement target) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {


        // TODO it doesn't get called as often as the methods in ShouldReference so it would be convenient if some static
        // information could be passed to it or created in its constructor

        // TODO it should only create references for @should PsiDocTags

        PsiDocTag psiDocTag = (PsiDocTag) element;
        if (BddUtil.isValidShouldTag(psiDocTag)) {


            // TODO create TestClass and TestMethod

            //  find the target test method
            PsiElement parentPsiClass = psiDocTag;
            do {
                parentPsiClass = parentPsiClass.getParent();
            } while (!(parentPsiClass instanceof PsiClass));

            TestClass testClass;
            try {
                testClass = BDDCore.createTestClass((PsiClass) parentPsiClass);
            } catch (TestFrameworkNotConfigured testFrameworkNotConfigured) {
                LOGGER.warn("Trying to resolve test methods but no framework is configured");
                return NO_REFERENCES;
            }
            List<TestMethod> allMethods = testClass.getAllMethods();

            TestMethod testMethod = null;
            for (TestMethod item : allMethods) {
                PsiDocTag backingTag = ((TestMethodImpl) item).getBackingTag();
                if (backingTag.equals(psiDocTag)) {
                    testMethod = item;
                }
            }
            ShouldReference shouldReference = new ShouldReference(psiDocTag, testMethod);
            return new PsiReference[]{shouldReference};
        } else {
            return NO_REFERENCES;
        }
    }
}
