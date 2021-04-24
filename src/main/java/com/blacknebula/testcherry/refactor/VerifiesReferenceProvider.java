package com.blacknebula.testcherry.refactor;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

public class VerifiesReferenceProvider extends PsiReferenceProvider {

    public static final VerifiesReferenceProvider INSTANCE = new VerifiesReferenceProvider();
    private static final PsiReference[] NO_REFERENCES = new PsiReference[0];
    private static final Logger LOGGER = Logger.getInstance(VerifiesReferenceProvider.class);

    private VerifiesReferenceProvider() {
    }

    /**
     * Static factory that returns singleton value
     *
     * @return Singleton instance of {@link VerifiesReferenceProvider}
     */
    public static VerifiesReferenceProvider getInstance() {
        return INSTANCE;
    }


    @Override
    public PsiReference @NotNull [] getReferencesByElement(@NotNull final PsiElement element, @NotNull final ProcessingContext context) {
        return new PsiReference[0];
    }
}
