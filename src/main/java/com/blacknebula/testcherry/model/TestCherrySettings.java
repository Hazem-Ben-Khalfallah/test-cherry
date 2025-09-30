package com.blacknebula.testcherry.model;

import com.blacknebula.testcherry.testframework.NamingConvention;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;

@State(
        name = "TestCherrySettings",
        storages = {@Storage("$WORKSPACE_FILE$")}
)
@Service(Service.Level.PROJECT)
public final class TestCherrySettings implements PersistentStateComponent<TestCherrySettings> { // TODO research about idea ProjectComponent interface


    String testFramework;
    NamingConvention namingConvention;
    // Whether to use JUnit 5 @DisplayName when available
    boolean useDescriptiveName;

    public TestCherrySettings() {
        testFramework = "";
        // Default to Camel Case naming when not previously set
        namingConvention = NamingConvention.CAMEL_CASE_NAMING;
    }

    /**
     * Return an instance of TestCherrySettings which holds plugin preferences as testFramework
     *
     */
    public static TestCherrySettings getInstance(Project project) {
        return project.getService(TestCherrySettings.class);
    }

    public String getTestFramework() {
        return testFramework;
    }

    public void setTestFramework(String testFramework) {
        this.testFramework = testFramework;
    }

    public NamingConvention getNamingConvention() {
        return namingConvention;
    }

    public void setNamingConvention(final NamingConvention namingConvention) {
        this.namingConvention = namingConvention;
    }

    // Accessors for the checkbox option
    public boolean isUseDescriptiveName() {
        return useDescriptiveName;
    }

    public void setUseDescriptiveName(boolean useDescriptiveName) {
        this.useDescriptiveName = useDescriptiveName;
    }

    @Override
    public TestCherrySettings getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull TestCherrySettings state) {
        XmlSerializerUtil.copyBean(state, this);
        // Backward compatibility: if namingConvention was never set in older state, default it
        if (this.namingConvention == null) {
            this.namingConvention = NamingConvention.CAMEL_CASE_NAMING;
        }
    }


}
