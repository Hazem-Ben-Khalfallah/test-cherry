package com.blacknebula.testcherry.model;

import java.util.ArrayList;
import java.util.List;

import com.blacknebula.testcherry.testframework.TestFrameworkStrategy;
import com.blacknebula.testcherry.util.BddUtil;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.impl.source.jsp.jspJava.JspClass;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.javadoc.PsiDocTag;


/**
 * User: JHABLUTZEL
 * Date: 29/10/2010
 * Time: 08:57:26 AM
 */
public class TestClassImpl implements TestClass {

    private final PsiClass sutClass;
    private final TestFrameworkStrategy frameworkStrategy;
    List<TestMethod> testMethods;


    private TestClassImpl(PsiClass psiClass, TestFrameworkStrategy frameworkStrategy) { // package protected

        //  check for unsupported classes
        if (psiClass instanceof JspClass) {
            throw new IllegalArgumentException("Unsupported class: " + psiClass.getClass().getName());
        }

        //  popular TestClass con TestMethods
        this.sutClass = psiClass;

        this.frameworkStrategy = frameworkStrategy;

        // init a refactor to the current project
        findAndInitializeAllTestMethods(psiClass);
    }

    /**
     * Initializes all test methods from psiClass
     *
     * @param psiClass
     */
    private void findAndInitializeAllTestMethods(PsiClass psiClass) {
        //   iterar sobre los metodos de la clase
        PsiMethod[] methods = psiClass.getMethods();
        ArrayList<TestMethod> array = new ArrayList<TestMethod>();
        //  iterar sobre los metodos
        for (PsiMethod method : methods) {


            //  iterar sobre los comentarios del javadoc
            PsiDocComment comment = method.getDocComment();
            if (comment == null) { // if no doc comment
                continue;
            }
            PsiDocTag[] tags = comment.getTags();
            //   iterar sobre los comentarios del javadoc
            for (PsiDocTag tag : tags) {


                //  comprobar que el tag sea del tipo should
                if (BddUtil.isValidShouldTag(tag)) {
                    // TODO change it to receive a string instead of the tag, or to test if the tag
                    // have empty description
                    TestMethod tm = TestMethodImpl.newInstance(tag, this, this.frameworkStrategy);
                    array.add(tm);
                }
            }
        }

        this.testMethods = array;
    }

    /**
     * Static factory method.
     * Effective Java item 1
     *
     * @param psiClass
     * @param frameworkStrategy
     * @return
     */
    static TestClassImpl newInstance(PsiClass psiClass, TestFrameworkStrategy frameworkStrategy) {
        return new TestClassImpl(psiClass, frameworkStrategy);
    }

    public List<TestMethod> getAllMethods() {
        return testMethods;
    }

    public void create(PsiDirectory sourceRoot) {

        frameworkStrategy.createBackingTestClass(sutClass, sourceRoot);

    }


    public boolean reallyExists() {
        PsiClass psiClass = frameworkStrategy.findBackingPsiClass(sutClass);

        return psiClass != null;

    }

    public PsiClass getBackingElement() {
        return frameworkStrategy.findBackingPsiClass(sutClass);
    }


    public PsiClass getClassUnderTest() {
        return this.sutClass;
    }

    @Override
    public TestFrameworkStrategy getTestFrameworkStrategy() {
        return frameworkStrategy;
    }
}
