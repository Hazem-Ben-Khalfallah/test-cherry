package com.blacknebula.javatestgenerator.model;

import com.blacknebula.javatestgenerator.testframework.*;
import com.intellij.psi.PsiElement;

/**
 * User: jhe
 */
public interface TestMember {

    TestFrameworkStrategy getTestFrameworkStrategy();

    /**
     * This methods returns the PsiElement backing this TestMember
     *
     * @return
     * @should return the supporting PsiElement for this test member
     */
    PsiElement getBackingElement();

}
