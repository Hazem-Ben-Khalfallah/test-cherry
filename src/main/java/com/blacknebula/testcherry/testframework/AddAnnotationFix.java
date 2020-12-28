package com.blacknebula.testcherry.testframework;

import com.intellij.codeInsight.intention.AddAnnotationPsiFix;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiModifierListOwner;
import com.intellij.psi.PsiNameValuePair;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

/**
 * @author ven
 */
public class AddAnnotationFix extends AddAnnotationPsiFix implements IntentionAction {
    public AddAnnotationFix(@NotNull String fqn, @NotNull PsiModifierListOwner modifierListOwner, String @NotNull ... annotationsToRemove) {
        this(fqn, modifierListOwner, PsiNameValuePair.EMPTY_ARRAY, annotationsToRemove);
    }

    public AddAnnotationFix(@NotNull String fqn,
                            @NotNull PsiModifierListOwner modifierListOwner,
                            PsiNameValuePair @NotNull [] values,
                            String @NotNull ... annotationsToRemove) {
        super(fqn, modifierListOwner, values, annotationsToRemove);
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
        if (InjectedLanguageManager.getInstance(project).isInjectedFragment(file)) {
            PsiElement psiElement = getStartElement();
            if (psiElement == null || psiElement.getContainingFile() != file) return false;
        }
        return isAvailable();
    }

    public boolean isAvailable(@NotNull Project project, PsiFile file) {
        return isAvailable(project, null, file);
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
        applyFix();
    }

    public void invoke() throws IncorrectOperationException {
        applyFix();
    }
}