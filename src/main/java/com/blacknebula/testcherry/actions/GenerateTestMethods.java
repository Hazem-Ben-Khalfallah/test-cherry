package com.blacknebula.testcherry.actions;

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
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

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

    @Nullable
    private static PsiClass getSubjectClass(Editor editor, DataContext dataContext) {
        PsiFile file = LangDataKeys.PSI_FILE.getData(dataContext);
        if (file == null) return null;

        int offset = editor.getCaretModel().getOffset();
        PsiElement element = file.findElementAt(offset);

        PsiClass parentPsiClass = BddUtil.getParentEligibleForTestingPsiClass(element);

        return parentPsiClass;

    }

    /**
     * It allows the user to create a test in the directory he chooses (test or production)
     *
     * @param e
     * @should process inmediately upper class if caret is at anonymous class
     */
    public void actionPerformed(AnActionEvent e) {

        // todo ADD support for unit testing, showing no ui, use ApplicationManager.getApplication().isUnitTestMode()
        DataContext dataContext = e.getDataContext();

        //  to get the current project
        final Project project = PlatformDataKeys.PROJECT.getData(dataContext);
        Editor editor = getEditor(dataContext);
        TestCherrySettings casesSettings = TestCherrySettings.getInstance(project);

        //  prompt to choose the strategy if it haven't been choosen before
        String testFrameworkProperty;
        if (ApplicationManager.getApplication().isUnitTestMode()) {
            testFrameworkProperty = "JUNIT3";
        } else {
            testFrameworkProperty = casesSettings.getTestFramework();

            if (StringUtils.isEmpty(testFrameworkProperty)) { //  it haven't been defined yet

                ExtensionPointName<ConfigurableEP> extensionPoint = ExtensionPointName.create("com.intellij.projectConfigurable");
                ConfigurableEP[] extensions = extensionPoint.getExtensions();
                for (ConfigurableEP component : extensions) {
                    Configurable configurable = (Configurable) component.createConfigurable();
                    if (configurable instanceof TestCherryConfigurable) {
                        ShowSettingsUtil.getInstance().editConfigurable(project, configurable);
                        break;
                    }
                }

                //  verify if something has been selected, if not just skip
                //  overwrite s variable
                testFrameworkProperty = casesSettings.getTestFramework();
                if (StringUtils.isEmpty(testFrameworkProperty)) {

                    //  show dialog displaying that there is no framework selection
                    Messages.showMessageDialog(TestCherryBundle.message("plugin.testCherry.framework.notselected.desc"),
                            TestCherryBundle.message("plugin.testCherry.framework.notselected"),
                            Messages.getWarningIcon());

                    return;
                }
            }

        }

        PsiClass psiClass = getSubjectClass(editor, dataContext);

        if (psiClass != null) {
            //  create test class for this psiClass

            //  get the current test framework strategy from settings


            // TODO replace it by strong typed way to determine the framework
            TestFrameworkStrategy tfs = SupportedFrameworks.getStrategyForFramework(project, testFrameworkProperty, casesSettings.getNamingConvention());

            final TestClass testClass = BDDCore.createTestClass(psiClass, tfs);


            ArrayList<ClassMember> array = new ArrayList<ClassMember>();

            List<TestMethod> allMethodsInOriginClass = testClass.getAllMethods();
            boolean createParent = false;
            //  ensure if parent exists
            PsiDirectory destinationRoot = null;
            final List<TestMethod> methodsToCreate = new ArrayList<TestMethod>();

            if (ApplicationManager.getApplication().isUnitTestMode()) {
                // in unit test mode it will create test methods for all should annotations
                methodsToCreate.addAll(allMethodsInOriginClass);
            } else {

                // TODO if methods is empty show message dialog, or disable button to generate
                for (TestMethod method : allMethodsInOriginClass) {

                    if (!method.reallyExists()) {
                        //  crear a psiDocAnnotation para cada metodo no existente
                        PsiDocAnnotationMember member = new PsiDocAnnotationMember(method);
                        array.add(member);
                    }
                }

                ClassMember[] classMembers = array.toArray(new ClassMember[array.size()]);
                MemberChooser<ClassMember> chooser = new MemberChooser<>(classMembers, false, true, project);
                chooser.setTitle("Choose should annotations");
                chooser.setCopyJavadocVisible(false);
                chooser.show();
                final List<ClassMember> selectedElements = chooser.getSelectedElements();

                if (isEmpty(selectedElements)) {
                    //  canceled or nothing selected
                    return;
                }


                for (ClassMember selectedElement : selectedElements) {
                    if (selectedElement instanceof PsiDocAnnotationMember) {
                        PsiDocAnnotationMember member = (PsiDocAnnotationMember) selectedElement;
                        methodsToCreate.add(member.getTestMethod());
                    }
                }

                // ensure parent exists
                if (!testClass.reallyExists()) {

                    //   otherwise allow to create in specified test sources root
                    VirtualFile[] sourceRoots = ProjectRootManager.getInstance(project).getContentSourceRoots();

                    //  get a list of all test roots
                    final PsiManager manager = PsiManager.getInstance(project);
                    List<PsiDirectory> allTestRoots = new ArrayList<>(2);
                    for (VirtualFile sourceRoot : sourceRoots) {
                        if (sourceRoot.isDirectory()) {
                            PsiDirectory directory = manager.findDirectory(sourceRoot);
                            if (directory != null) { // only source roots that really exists right now
                                allTestRoots.add(directory);
                            }
                        }
                    }

                    if (isEmpty(allTestRoots)) {
                        //  just cancel
                        return;
                    }

                    //  only display if more than one source root
                    if (allTestRoots.size() > 1) {
                        DirectoryChooser fileChooser = new DirectoryChooser(project);
                        fileChooser.setTitle(IdeBundle.message("title.choose.destination.directory"));
                        fileChooser.fillList(allTestRoots.toArray(new PsiDirectory[allTestRoots.size()]), null, project, "");
                        fileChooser.show();
                        destinationRoot = fileChooser.isOK() ? fileChooser.getSelectedDirectory() : null;
                    } else {
                        destinationRoot = allTestRoots.get(0);
                    }


                    if (destinationRoot != null) {
                        createParent = true;
                    } else {
                        //  just cancel
                        return;
                    }

                }
            }

            //  if backing test class exists, just create the methods in the same
            //  para cada uno de los seleccionados llamar a create
            //  create an appropiate command name
            final String commandName = TestCherryBundle.message("plugin.testCherry.creatingtestcase", testClass.getClassUnderTest().getName());
            final PsiDirectory finalDestinationRoot = destinationRoot;
            final boolean finalCreateParent = createParent;
            WriteCommandAction.writeCommandAction(project)
                    .withName(commandName)
                    .run(() -> {
                        LocalHistoryAction action = LocalHistoryAction.NULL;
                        //  wrap this with error management
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
                            //  if something has been created jump to the last created test method, this is 'lastTestMethod'
                            if (lastTestMethod != null) {
                                lastTestMethod.navigate();
                            }
                        } finally {
                            action.finish();
                        }

                    });


        }


    }

    public void update(Editor editor, Presentation presentation, DataContext dataContext) {
        //  si no hay ninguna clase en el editor se deberia desactivar la accion
        presentation.setEnabled(getSubjectClass(editor, dataContext) != null);
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

    protected Editor getEditor(final DataContext dataContext) {
        return PlatformDataKeys.EDITOR.getData(dataContext);
    }

}
