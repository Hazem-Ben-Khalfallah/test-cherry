package com.blacknebula.testcherry.codeinsight;

import com.blacknebula.testcherry.model.TestCherrySettings;
import com.blacknebula.testcherry.testframework.NamingConvention;
import com.blacknebula.testcherry.testframework.SupportedFrameworks;
import com.intellij.openapi.options.BaseConfigurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import org.jetbrains.annotations.Nls;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * User: Jaime Hablutzel
 */
public class TestCherryConfigurable extends BaseConfigurable implements SearchableConfigurable {


    private static final String EMPTY_STRING = "";
    private final Project myProject;
    private MyComponent myComponent;


    public TestCherryConfigurable(Project myProject) {
        this.myProject = myProject;
    }

    @Override
    public String getId() {
        return getDisplayName();  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Runnable enableSearch(String option) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "Test Cherry";
    }


    @Override
    public String getHelpTopic() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public JComponent createComponent() {
        List<String> supportedFrameworkNames = new ArrayList<>();
        supportedFrameworkNames.add("-");
        SupportedFrameworks[] frameworks = SupportedFrameworks.values();
        for (SupportedFrameworks framework : frameworks) {
            supportedFrameworkNames.add(framework.toString());
        }

        List<NamingConvention> namingConventionNames = new ArrayList<>(Arrays.asList(NamingConvention.values()));

        DefaultComboBoxModel aModel = new DefaultComboBoxModel(supportedFrameworkNames.toArray());
        DefaultComboBoxModel namingConventionComboBox = new DefaultComboBoxModel(namingConventionNames.toArray());

        TestCherrySettings casesSettings = TestCherrySettings.getInstance(myProject);

        myComponent = new MyComponent();

        if (casesSettings != null) {
            addTestingTypeComboBoxItems(aModel, casesSettings);
            addNamingConventionComboBoxItems(namingConventionComboBox, casesSettings);
        }

        //To change body of implemented methods use File | Settings | File Templates.
        return myComponent.getPanel();
    }

    @Override
    public void apply() {

        //  get settings holder
        TestCherrySettings casesSettings = TestCherrySettings.getInstance(myProject);
        //  persist currently selected test framework
        String testFramework = myComponent.comboBoxTestType.getSelectedItem().toString();
        if (!testFramework.equals("-")) {
            casesSettings.setTestFramework(testFramework);
        } else {
            casesSettings.setTestFramework(EMPTY_STRING);
        }

        NamingConvention namingConvention =  (NamingConvention) myComponent.comboBoxNamingConvention.getSelectedItem();
        casesSettings.setNamingConvention(namingConvention);
    }

    @Override
    public void reset() {

    }

    @Override
    public boolean isModified() {
        TestCherrySettings casesSettings = TestCherrySettings.getInstance(myProject);
        return isTestTypeModified(casesSettings) || isNamingConventionModified(casesSettings);
    }

    private boolean isTestTypeModified(final TestCherrySettings casesSettings) {
        String currentTestTypeValue = (String) myComponent.comboBoxTestType.getSelectedItem();
        String savedTestTypeValue = casesSettings.getTestFramework();
        if (currentTestTypeValue.equals("-")) {
            return !EMPTY_STRING.equals(savedTestTypeValue);
        } else {
            return !currentTestTypeValue.equals(savedTestTypeValue);
        }
    }

    private boolean isNamingConventionModified(final TestCherrySettings casesSettings) {
        NamingConvention selectedNamingConvention = (NamingConvention) myComponent.comboBoxNamingConvention.getSelectedItem();
        NamingConvention savedNamingConventionValue = casesSettings.getNamingConvention();
        return selectedNamingConvention != savedNamingConventionValue;
    }

    @Override
    public void disposeUIResources() {
    }

    private void addTestingTypeComboBoxItems(DefaultComboBoxModel aModel, TestCherrySettings casesSettings) {
        String testFramework = casesSettings.getTestFramework();
        if (!testFramework.equals(EMPTY_STRING)) {
            aModel.setSelectedItem(testFramework);
        }
        myComponent.setTestingTypeModel(aModel);
    }

    private void addNamingConventionComboBoxItems(DefaultComboBoxModel aModel, TestCherrySettings casesSettings) {
        NamingConvention namingConvention = casesSettings.getNamingConvention();
        if (namingConvention != null) {
            aModel.setSelectedItem(namingConvention);
        }

        myComponent.setNamingConventionModel(aModel);
    }

    private static class MyComponent {

        private final JComboBox<String> comboBoxTestType = new ComboBox();
        private final JComboBox<NamingConvention> comboBoxNamingConvention = new ComboBox<>();
        private final JPanel panel = new JPanel();

        private MyComponent() {
            panel.add(comboBoxTestType);
            panel.add(comboBoxNamingConvention);
        }

        private MyComponent setTestingTypeModel(ComboBoxModel<String> model) {
            comboBoxTestType.setModel(model);
            return this;
        }

        private MyComponent setNamingConventionModel(ComboBoxModel<NamingConvention> model) {
            comboBoxNamingConvention.setModel(model);
            return this;
        }

        public JPanel getPanel() {
            return panel;
        }
    }

}
