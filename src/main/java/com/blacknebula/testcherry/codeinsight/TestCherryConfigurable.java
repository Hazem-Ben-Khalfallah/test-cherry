package com.blacknebula.testcherry.codeinsight;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.blacknebula.testcherry.model.TestCherrySettings;
import com.blacknebula.testcherry.testframework.NamingConvention;
import com.blacknebula.testcherry.testframework.SupportedFrameworks;
import com.intellij.openapi.options.BaseConfigurable;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import javax.swing.*;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import java.awt.event.ItemEvent;
import com.intellij.util.ui.FormBuilder;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import com.intellij.ui.TitledSeparator;

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
    public @NotNull String getId() {
        return getDisplayName();  //To change body of implemented methods use File | Settings | File Templates.
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
    public Runnable enableSearch(String option) {
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

        TestCherrySettings casesSettings = TestCherrySettings.getInstance(myProject);

        myComponent = new MyComponent();

        if (casesSettings != null) {
            // Framework dropdown items
            DefaultComboBoxModel<String> aModel = new DefaultComboBoxModel<>(supportedFrameworkNames.toArray(new String[0]));
            addTestingTypeComboBoxItems(aModel, casesSettings);

            // Naming convention radios
            addNamingConventionOptions(casesSettings);

            // init checkbox state from settings
            myComponent.checkBoxUseDisplayName.setSelected(casesSettings.isUseDescriptiveName());
            // if current framework is not JUNIT5, force unchecked (effective false)
            Object current = myComponent.comboBoxTestType.getSelectedItem();
            boolean isJunit5 = current != null && SupportedFrameworks.JUNIT5.name().equals(current.toString());
            if (!isJunit5) {
                myComponent.checkBoxUseDisplayName.setSelected(false);
            }
            // set initial enabled/visible state
            myComponent.checkBoxUseDisplayName.setEnabled(isJunit5);
            myComponent.checkBoxUseDisplayName.setVisible(isJunit5);

            // dynamically update on dropdown change
            myComponent.comboBoxTestType.addItemListener(e -> {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    Object selected = e.getItem();
                    boolean nowJunit5 = selected != null && SupportedFrameworks.JUNIT5.name().equals(selected.toString());
                    myComponent.checkBoxUseDisplayName.setEnabled(nowJunit5);
                    myComponent.checkBoxUseDisplayName.setVisible(nowJunit5);
                    if (nowJunit5) {
                        // restore saved value when switching to JUNIT5
                        boolean saved = TestCherrySettings.getInstance(myProject).isUseDescriptiveName();
                        myComponent.checkBoxUseDisplayName.setSelected(saved);
                    } else {
                        // force unchecked when non-JUNIT5
                        myComponent.checkBoxUseDisplayName.setSelected(false);
                    }
                    // refresh layout after visibility change
                    myComponent.getPanel().revalidate();
                    myComponent.getPanel().repaint();
                }
            });
        }

        // Anchor to top-left
        JPanel container = new JPanel(new BorderLayout());
        container.add(myComponent.getPanel(), BorderLayout.NORTH);
        return container;
    }

    @Override
    public void apply() {

        //  get settings holder
        TestCherrySettings casesSettings = TestCherrySettings.getInstance(myProject);
        //  persist currently selected test framework
        Object selectedItem = myComponent.comboBoxTestType.getSelectedItem();
        String testFramework = selectedItem != null ? selectedItem.toString() : "-";
        if (!testFramework.equals("-")) {
            casesSettings.setTestFramework(testFramework);
        } else {
            casesSettings.setTestFramework(EMPTY_STRING);
        }

        // naming convention from radio buttons
        NamingConvention namingConvention = myComponent.getSelectedNamingConvention();
        casesSettings.setNamingConvention(namingConvention);
        // persist checkbox state only when framework is JUNIT5, else assume false
        boolean effectiveUseDisplayName = SupportedFrameworks.JUNIT5.name().equals(testFramework) && myComponent.checkBoxUseDisplayName.isSelected();
        casesSettings.setUseDescriptiveName(effectiveUseDisplayName);
    }

    @Override
    public void reset() {
        TestCherrySettings casesSettings = TestCherrySettings.getInstance(myProject);
        if (casesSettings != null && myComponent != null) {
            Object current = myComponent.comboBoxTestType.getSelectedItem();
            boolean isJunit5 = current != null && SupportedFrameworks.JUNIT5.name().equals(current.toString());
            if (isJunit5) {
                myComponent.checkBoxUseDisplayName.setSelected(casesSettings.isUseDescriptiveName());
            } else {
                myComponent.checkBoxUseDisplayName.setSelected(false);
            }
            myComponent.checkBoxUseDisplayName.setEnabled(isJunit5);
            myComponent.checkBoxUseDisplayName.setVisible(isJunit5);

            // reset naming convention
            myComponent.setSelectedNamingConvention(casesSettings.getNamingConvention());

            myComponent.getPanel().revalidate();
            myComponent.getPanel().repaint();
        }
    }

    @Override
    public void disposeUIResources() {
    }

    private void addTestingTypeComboBoxItems(DefaultComboBoxModel<String> aModel, TestCherrySettings casesSettings) {
        String testFramework = casesSettings.getTestFramework();
        if (!testFramework.equals(EMPTY_STRING)) {
            aModel.setSelectedItem(testFramework);
        }
        myComponent.setTestingTypeModel(aModel);
    }

    private void addNamingConventionOptions(TestCherrySettings casesSettings) {
        NamingConvention namingConvention = casesSettings.getNamingConvention();
        if (namingConvention != null) {
            myComponent.setSelectedNamingConvention(namingConvention);
        }
    }

    @Override
    public boolean isModified() {
        TestCherrySettings casesSettings = TestCherrySettings.getInstance(myProject);
        return isTestTypeModified(casesSettings) || isNamingConventionModified(casesSettings)
                || isUseDisplayNameModified(casesSettings);
    }

    private boolean isTestTypeModified(final TestCherrySettings casesSettings) {
        String currentTestTypeValue = (String) myComponent.comboBoxTestType.getSelectedItem();
        String savedTestTypeValue = casesSettings.getTestFramework();
        if ("-".equals(currentTestTypeValue)) {
            return !EMPTY_STRING.equals(savedTestTypeValue);
        } else {
            return currentTestTypeValue != null && !currentTestTypeValue.equals(savedTestTypeValue);
        }
    }

    private boolean isNamingConventionModified(final TestCherrySettings casesSettings) {
        NamingConvention selectedNamingConvention = myComponent.getSelectedNamingConvention();
        NamingConvention savedNamingConventionValue = casesSettings.getNamingConvention();
        return selectedNamingConvention != savedNamingConventionValue;
    }

    private boolean isUseDisplayNameModified(final TestCherrySettings casesSettings) {
        Object current = myComponent.comboBoxTestType.getSelectedItem();
        boolean effectiveSelected = current != null && SupportedFrameworks.JUNIT5.name().equals(current.toString()) && myComponent.checkBoxUseDisplayName.isSelected();
        boolean saved = casesSettings.isUseDescriptiveName();
        return effectiveSelected != saved;
    }

    private static class MyComponent {

        private final JComboBox<String> comboBoxTestType = new ComboBox<>();
        private final JCheckBox checkBoxUseDisplayName = new JCheckBox("Use description as display name");

        private final Map<NamingConvention, JRadioButton> namingConventionRadios = new LinkedHashMap<>();

        private final JPanel panel;

        private MyComponent() {
            // Build naming convention radios
            JPanel namingConventionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            for (NamingConvention value : NamingConvention.values()) {
                JRadioButton rb = new JRadioButton(value.toString());
                ButtonGroup namingConventionGroup = new ButtonGroup();
                namingConventionGroup.add(rb);
                namingConventionRadios.put(value, rb);
                namingConventionPanel.add(rb);
            }

            // Framework row combines dropdown and checkbox
            JPanel frameworkRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            frameworkRow.add(comboBoxTestType);
            frameworkRow.add(checkBoxUseDisplayName);

            // Form with two groups (using TitledSeparator for compatibility)
            panel = FormBuilder.createFormBuilder()
                    .addComponent(new TitledSeparator("Framework Settings"))
                    .addLabeledComponent("Framework:", frameworkRow)
                    .addComponent(new TitledSeparator("Generation Settings"))
                    .addLabeledComponent("Naming convension:", namingConventionPanel)
                    .getPanel();
        }

        private MyComponent setTestingTypeModel(ComboBoxModel<String> model) {
            comboBoxTestType.setModel(model);
            return this;
        }

        private void setSelectedNamingConvention(NamingConvention convention) {
            JRadioButton rb = namingConventionRadios.get(convention);
            if (rb != null) {
                rb.setSelected(true);
            }
        }

        private NamingConvention getSelectedNamingConvention() {
            for (Map.Entry<NamingConvention, JRadioButton> e : namingConventionRadios.entrySet()) {
                if (e.getValue().isSelected()) {
                    return e.getKey();
                }
            }
            // default to first
            return NamingConvention.values().length > 0 ? NamingConvention.values()[0] : null;
        }

        public JPanel getPanel() {
            return panel;
        }
    }

}
