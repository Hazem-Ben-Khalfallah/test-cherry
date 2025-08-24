package com.blacknebula.testcherry.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.blacknebula.testcherry.TestCherryBundle;
import com.blacknebula.testcherry.codeinsight.TestCherryConfigurable;
import com.blacknebula.testcherry.codeinsight.generation.PsiDocAnnotationMember;
import com.blacknebula.testcherry.model.BDDCore;
import com.blacknebula.testcherry.model.TestCherrySettings;
import com.blacknebula.testcherry.model.TestClass;
import com.blacknebula.testcherry.model.TestMethod;
import com.blacknebula.testcherry.testframework.SupportedFrameworks;
import com.blacknebula.testcherry.testframework.TestFrameworkStrategy;
import com.blacknebula.testcherry.util.BddUtil;
import com.intellij.codeInsight.generation.ClassMember;
import com.intellij.history.LocalHistory;
import com.intellij.history.LocalHistoryAction;
import com.intellij.ide.IdeBundle;
import com.intellij.ide.util.DirectoryChooser;
import com.intellij.ide.util.MemberChooser;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurableEP;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import static org.apache.commons.collections.CollectionUtils.isEmpty;

/**
 * User: JHABLUTZEL
 * Date: 20/10/2010
 * Time: 12:27:20 PM
 */
public class GenerateTestMethods extends AnAction {
    private static final Logger LOG = Logger.getInstance(GenerateTestMethods.class);


    public GenerateTestMethods() {
        super("Generate Test Methods",
                "Generate test methods for current file",
                IconLoader.getIcon("/images/logo.png", GenerateTestMethods.class));
    }

    @Override
    public void update(AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        DataContext dataContext = e.getDataContext();
        Editor editor = getEditor(dataContext);
        if (editor == null) {
            presentation.setEnabled(false);
        } else {
            update(editor, presentation, dataContext);
        }
    }

    /**
     * It allows the user to create a test in the directory he chooses (test or production)
     *
     * @param e
     * @should process inmediately upper class if caret is at anonymous class
     */
    @Override
    public void actionPerformed(AnActionEvent e) {
        DataContext dataContext = e.getDataContext();
        final Project project = e.getProject();

        if (project == null) return;

        Editor editor = getEditor(dataContext);
        if (editor == null) return;

        TestCherrySettings casesSettings = TestCherrySettings.getInstance(project);

        // Determine the test framework
        String testFrameworkProperty;
        if (ApplicationManager.getApplication().isUnitTestMode()) {
            testFrameworkProperty = "JUNIT3";
        } else {
            testFrameworkProperty = casesSettings.getTestFramework();
        }

        // Prompt for framework selection if not set
        if (StringUtils.isEmpty(testFrameworkProperty)) {
            ShowSettingsUtil.getInstance().showSettingsDialog(project,
                    TestCherryBundle.message("plugin.testCherry.title")
            );
            return;
        }

        // Get the current class under the caret
        PsiClass psiClass = getSubjectClass(editor, dataContext);
        if (psiClass == null) return;

        // Get the test framework strategy
        TestFrameworkStrategy tfs = SupportedFrameworks.getStrategyForFramework(
                project, testFrameworkProperty, casesSettings.getNamingConvention()
        );

        final TestClass testClass = BDDCore.createTestClass(psiClass, tfs);

        // Collect all test methods from the origin class
        List<TestMethod> allMethodsInOriginClass = testClass.getAllMethods();
        ArrayList<ClassMember> array = new ArrayList<>();

        // Only add methods that do not already exist
        for (TestMethod method : allMethodsInOriginClass) {
            if (!method.reallyExists()) {
                PsiDocAnnotationMember member = new PsiDocAnnotationMember(method);
                array.add(member);
            }
        }

        if (array.isEmpty()) {
            Messages.showMessageDialog(
                    TestCherryBundle.message("plugin.testCherry.framework-not-selected.desc"),
                    TestCherryBundle.message("plugin.testCherry.framework-not-selected.title"),
                    Messages.getInformationIcon()
            );
            return;
        }

        // Show chooser dialog for user to select which methods to generate
        ClassMember[] classMembers = array.toArray(new ClassMember[0]);
        MemberChooser<ClassMember> chooser = new MemberChooser<>(classMembers, false, true, project);
        chooser.setTitle("Choose should annotations");
        chooser.setCopyJavadocVisible(false);
        chooser.show();
        final List<ClassMember> selectedElements = chooser.getSelectedElements();

        if (selectedElements == null || selectedElements.isEmpty()) {
            // Cancelled or nothing selected
            return;
        }

        // Prepare methods to create
        final List<TestMethod> methodsToCreate = new ArrayList<>();
        for (ClassMember selectedElement : selectedElements) {
            if (selectedElement instanceof PsiDocAnnotationMember member) {
                methodsToCreate.add(member.getTestMethod());
            }
        }

        // Ensure parent test class exists
        boolean createParent = !testClass.reallyExists();
        PsiDirectory destinationRoot = null; // You may want to determine the correct root here

        // Write action to create test methods
        final String commandName = TestCherryBundle.message(
                "plugin.testCherry.creating-test-case",
                testClass.getClassUnderTest().getName()
        );
        final PsiDirectory finalDestinationRoot = destinationRoot;
        final boolean finalCreateParent = createParent;

        WriteCommandAction.writeCommandAction(project)
                .withName(commandName)
                .run(() -> {
                    LocalHistoryAction action = LocalHistoryAction.NULL;
                    try {
                        action = LocalHistory.getInstance().startAction(commandName);
                        if (finalCreateParent) {
                            testClass.create(finalDestinationRoot);
                        }
                        TestMethod lastTestMethod = null;
                        for (TestMethod testMethod : methodsToCreate) {
                            if (!testMethod.reallyExists()) {
                                testMethod.create();
                                lastTestMethod = testMethod;
                            }
                        }
                        // Navigate to the last created test method
                        if (lastTestMethod != null) {
                            lastTestMethod.navigate();
                        }
                    } finally {
                        action.finish();
                    }
                });
    }

    protected Editor getEditor(final DataContext dataContext) {
        return PlatformDataKeys.EDITOR.getData(dataContext);
    }

    private void update(Editor editor, Presentation presentation, DataContext dataContext) {
        // If there is no class in the editor, the action should be disabled.
        presentation.setEnabled(getSubjectClass(editor, dataContext) != null);
    }

    @Nullable
    private PsiClass getSubjectClass(Editor editor, DataContext dataContext) {
        final var file = getCurrentFile(editor, dataContext);
        if (file.isEmpty()) return null;

        int offset = editor != null ? editor.getCaretModel().getOffset() : 0;
        final var element = file.get().findElementAt(offset);
        return BddUtil.getParentEligibleForTestingPsiClass(element);
    }

    private Optional<PsiFile> getCurrentFile(Editor editor, DataContext dataContext) {
        PsiFile file = null;
        if (editor != null) {
            Project project = editor.getProject();
            if (project != null) {
                var document = editor.getDocument();
                file = PsiDocumentManager.getInstance(project).getPsiFile(document);
            }
        }
        if (file == null) {
            file = LangDataKeys.PSI_FILE.getData(dataContext);
        }
        return Optional.ofNullable(file);
    }

}
