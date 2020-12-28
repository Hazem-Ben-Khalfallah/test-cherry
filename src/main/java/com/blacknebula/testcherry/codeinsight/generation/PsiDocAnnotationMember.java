package com.blacknebula.testcherry.codeinsight.generation;

import com.blacknebula.testcherry.model.TestMethod;
import com.blacknebula.testcherry.model.TestMethodImpl;
import com.intellij.codeInsight.generation.ClassMemberWithElement;
import com.intellij.codeInsight.generation.MemberChooserObject;
import com.intellij.codeInsight.generation.PsiElementMemberChooserObject;
import com.intellij.codeInsight.generation.PsiMethodMember;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;

/**
 * TODO revisar bien esta clase y poner nombres mas adecuados a los miembros
 * <p>
 * User: JHABLUTZEL
 * Date: 22/10/2010
 * Time: 12:22:44 PM
 */
public class PsiDocAnnotationMember extends PsiElementMemberChooserObject implements ClassMemberWithElement {
    private final PsiElement psiDocTag;
    private final PsiMethod method;
    private final PsiMethodMember member;
    private final TestMethod testMethod;

    public PsiDocAnnotationMember(TestMethod tm) {
        super(((TestMethodImpl) tm).getBackingTag(), tm.getDescription());
        this.testMethod = tm;
        this.psiDocTag = ((TestMethodImpl) tm).getBackingTag();
        this.method = tm.getSutMethod();
        this.member = new PsiMethodMember(method);

    }

    public TestMethod getTestMethod() {
        return testMethod;
    }

    public PsiElement getElement() {
        return psiDocTag;
    }

    public MemberChooserObject getParentNodeDelegate() {
        return member;
    }
}
