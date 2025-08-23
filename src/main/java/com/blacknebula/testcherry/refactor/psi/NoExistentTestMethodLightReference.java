package com.blacknebula.testcherry.refactor.psi;

import com.blacknebula.testcherry.refactor.ShouldTagsAwareRenameProcessor;
import com.intellij.lang.Language;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.light.LightElement;

/**
 * This light PsiElement will be used when referencing to non existent
 * test method.
 * This way {@link ShouldTagsAwareRenameProcessor} is still able to find a valid refactor and rename a (at)should tag that haven't yet a test method created.
 * <p>
 * Created by jhe
 * Time: 14:14
 */
public class NoExistentTestMethodLightReference extends LightElement {


    public NoExistentTestMethodLightReference(PsiManager manager, Language language) {
        super(manager, language);
    }

    @Override
    public boolean isWritable() {
        return true; // true because we allow refactoring :D
    }

    @Override
    public String toString() {
        return "PsiBDDTestMethod:";
    }
}
