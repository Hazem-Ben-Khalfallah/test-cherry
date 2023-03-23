package com.blacknebula.testcherry.model;

import com.blacknebula.testcherry.testframework.NamingConvention;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;

@State(
        name = "TestCherrySettings",
        storages = {
                @Storage("$WORKSPACE_FILE$")}
)
public class TestCherrySettings implements PersistentStateComponent<TestCherrySettings> { // TODO research about idea ProjectComponent interface


    String testFramework;
    NamingConvention namingConvention;

    public TestCherrySettings() {
        testFramework = "";
    }

    /**
     * Return an instance of TestCherrySettings which holds plugin preferences as testFramework
     *
     * @param project
     * @return
     */
    public static TestCherrySettings getInstance(Project project) {
        return project.getComponent(TestCherrySettings.class);
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

    public TestCherrySettings getState() {
        return this;
    }

    public void loadState(TestCherrySettings state) {
        XmlSerializerUtil.copyBean(state, this);
    }


}
