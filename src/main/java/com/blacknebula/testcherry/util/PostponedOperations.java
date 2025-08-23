package com.blacknebula.testcherry.util;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

public class PostponedOperations {
    /**
     * This code will run after all documents are committed to PSI
     * Safe to read or modify PSI here
     */
    public static void performLater(@NotNull Project project, @NotNull PsiFile file, @NotNull Runnable runnable) {
        ensureAllPostponedOperationsAreCompleted(project, file);

        ApplicationManager.getApplication().runWriteAction(runnable);
    }

    public static void ensureAllPostponedOperationsAreCompleted(@NotNull Project project, PsiFile file) {
        // Ensure all postponed operations are completed
        var document = PsiDocumentManager.getInstance(project).getDocument(file);
        if (document != null) {
            PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(document);
        }
    }
}
