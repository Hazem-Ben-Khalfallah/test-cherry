package com.blacknebula.testcherry.refactor;

import com.blacknebula.testcherry.refactor.psi.NoExistentTestMethodLightReference;
import com.blacknebula.testcherry.refactor.rename.ShouldTagRenameDialog;
import com.intellij.codeInsight.TargetElementUtilBase;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.javadoc.PsiDocTag;
import com.intellij.refactoring.rename.RenameDialog;
import com.intellij.refactoring.rename.RenameJavaMethodProcessor;
import com.intellij.refactoring.rename.RenamePsiElementProcessor;
import org.jetbrains.annotations.NotNull;

/**
 * This {@link RenamePsiElementProcessor} will provide java methods renaming support
 * for any java method it will provide standard renaming, but for methods being renamed from a @should tag
 * it will provide renaming of the test method from the @should tag description
 * <p/>
 * <p/>
 * Creado por: jaime
 * 22/09/11
 */
public class ShouldTagsAwareRenameProcessor extends RenameJavaMethodProcessor {


    /**
     * its nameSuggestionContext isn't using the same logic as the refactor provider to find the element to rename from
     * the refactor, it is passing a whitespace when the cursor is after (at)should tag, so we should use the same logic
     * than the refactor provider to know where are we located in the editor.
     *
     * @should create ShouldTagRenameDialog instead of RenameDialog when element is a TestMethod got from a (at)should tag reference
     */
    @Override
    public RenameDialog createRenameDialog(Project project, PsiElement element, PsiElement nameSuggestionContext, Editor editor) {

        int offset = editor.getCaretModel().getOffset();
        PsiReference shouldRef = TargetElementUtilBase.findReference(editor, offset);

        //  add refactor to doctag to NoExistentTestMethodLightReference and make the ShouldTagRenameDialog aware of it

        // create custom rename Dialog
        if (shouldRef instanceof ShouldReference) { // we are coming from a javadoc

            // nameSuggestionContext isn't precise so we enforce the PsiDocTag to be the suggestion context
            return new ShouldTagRenameDialog(project, element, shouldRef.getElement(), editor, (PsiDocTag) shouldRef.getElement());

        } else {             //  else standard renaming
            return new RenameDialog(project, element, nameSuggestionContext, editor);
        }
    }


    @Override
    public boolean canProcessElement(@NotNull PsiElement element) {
        //  able to process PsiMethods and com.blacknebula.javatestgenerator.refactor.psi.NoExistentTestMethodLightReference, this is tags without test method created, or delegates to the parent

        return element instanceof NoExistentTestMethodLightReference || super.canProcessElement(element);

    }


    @Override
    public PsiElement substituteElementToRename(PsiElement element, Editor editor) {

        if (element instanceof NoExistentTestMethodLightReference) {
            return element;
        } else {

            return super.substituteElementToRename(element, editor);
        }
    }

    @Override
    public boolean isInplaceRenameSupported() {
        return false;
    }
}
