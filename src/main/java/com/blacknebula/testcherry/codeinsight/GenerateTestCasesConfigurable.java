package com.blacknebula.testcherry.codeinsight;

import com.blacknebula.testcherry.model.GenerateTestCasesSettings;
import com.blacknebula.testcherry.testframework.SupportedFrameworks;
import com.intellij.openapi.options.BaseConfigurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import org.jetbrains.annotations.Nls;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * User: Jaime Hablutzel
 */
public class GenerateTestCasesConfigurable extends BaseConfigurable implements SearchableConfigurable {


    private static final String EMPTY_STRING = "";
    private final Project myProject;
    private MyComponent myComponent;


    public GenerateTestCasesConfigurable(Project myProject) {
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
        List<String> strings = new ArrayList<String>();
        strings.add("-");
        SupportedFrameworks[] frameworks = SupportedFrameworks.values();
        for (SupportedFrameworks framework : frameworks) {
            strings.add(framework.toString());
        }

        DefaultComboBoxModel aModel = new DefaultComboBoxModel(strings.toArray());

        GenerateTestCasesSettings casesSettings = GenerateTestCasesSettings.getInstance(myProject);


        String testFramework = casesSettings.getTestFramework();
        if (!testFramework.equals(EMPTY_STRING)) {
            aModel.setSelectedItem(testFramework);
        }

        myComponent = new MyComponent()
                .setModel(aModel);

        //To change body of implemented methods use File | Settings | File Templates.
        return myComponent.getPanel();
    }

    @Override
    public void apply() throws ConfigurationException {

        //  get settings holder
        GenerateTestCasesSettings casesSettings = GenerateTestCasesSettings.getInstance(myProject);
        //  persist currently selected test framework
        String s = myComponent.comboBox.getSelectedItem().toString();
        if (!s.equals("-")) {
            casesSettings.setTestFramework(s);
        } else {
            casesSettings.setTestFramework(EMPTY_STRING);
        }

    }

    @Override
    public void reset() {

    }

    @Override
    public boolean isModified() {
        GenerateTestCasesSettings casesSettings = GenerateTestCasesSettings.getInstance(myProject);
        String o = (String) myComponent.comboBox.getSelectedItem();
        String s = casesSettings.getTestFramework();
        if (o.equals("-")) {
            return !s.equals(EMPTY_STRING);
        } else {
            return !o.equals(s);
        }
    }


    @Override
    public void disposeUIResources() {
    }

    private static class MyComponent {

        private JComboBox comboBox;
        private JPanel panel;

        private MyComponent() {
            comboBox = new ComboBox();
            panel = new JPanel();
            panel.add(comboBox);
        }

        private MyComponent setModel(ComboBoxModel model) {
            comboBox.setModel(model);
            return this;
        }

        public JPanel getPanel() {
            return panel;
        }
    }

}
