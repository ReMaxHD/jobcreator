package com.swisslog.jenkins.jobcreator.bindings;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The class InputParameter is a simple binding to the config.xml
 */
@XmlRootElement(name = "inputparameter")
@XmlAccessorType(XmlAccessType.FIELD)
public class InputParameterWm6 {

    private Boolean abortOnError;

    private Map<String, String> parameterMap = new HashMap<String, String>();

    private String projectName;
    private String parentView;

    private Boolean createMainJob;
    private Boolean createSonarJob;
    private Boolean createCleanupJob;
    private Boolean createIntegrationTestHsqlJob;
    private Boolean createIntegrationTestOracleJob;
    private Boolean createIntegrationTestSqlJob;

    private Boolean createSystemTestJobs;

    @XmlElement(name = "module")
    private List<InputParameterModule> modules;

    /**
     * @return the createSystemTestJobs
     */
    public Boolean getCreateSystemTestJobs() {
        return createSystemTestJobs;
    }

    /**
     * @param createSystemTestJobs
     *            the createSystemTestJobs to set
     */
    public void setCreateSystemTestJobs(Boolean createSystemTestJobs) {
        this.createSystemTestJobs = createSystemTestJobs;
    }

    /**
     * @return the abortOnError
     */
    public Boolean getAbortOnError() {
        return abortOnError;
    }

    /**
     * @param abortOnError
     *            the abortOnError to set
     */
    public void setAbortOnError(Boolean abortOnError) {
        this.abortOnError = abortOnError;
    }

    /**
     * @return the projectName
     */
    public String getProjectName() {
        return projectName;
    }

    /**
     * @param projectName
     *            the projectName to set
     */
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    /**
     * @return the parentView
     */
    public String getParentView() {
        return parentView;
    }

    /**
     * @param parentView
     *            the parentView to set
     */
    public void setParentView(String parentView) {
        this.parentView = parentView;
    }

    /**
     * @return the createMainJob
     */
    public Boolean getCreateMainJob() {
        return createMainJob;
    }

    /**
     * @param createMainJob
     *            the createMainJob to set
     */
    public void setCreateMainJob(Boolean createMainJob) {
        this.createMainJob = createMainJob;
    }

    public Boolean getCreateCleanupJob() {
        return createCleanupJob;
    }

    public Boolean getCreateIntegrationTestHsqlJob() {
        return createIntegrationTestHsqlJob;
    }

    public void setCreateIntegrationTestHsqlJob(Boolean createIntegrationTestHsqlJob) {
        this.createIntegrationTestHsqlJob = createIntegrationTestHsqlJob;
    }

    public Boolean getCreateIntegrationTestOracleJob() {
        return createIntegrationTestOracleJob;
    }

    public void setCreateIntegrationTestOracleJob(Boolean createIntegrationTestOracleJob) {
        this.createIntegrationTestOracleJob = createIntegrationTestOracleJob;
    }

    public Boolean getCreateIntegrationTestSqlJob() {
        return createIntegrationTestSqlJob;
    }

    public void setCreateIntegrationTestSqlJob(Boolean createIntegrationTestSqlJob) {
        this.createIntegrationTestSqlJob = createIntegrationTestSqlJob;
    }

    /**
     * @return the createSonarJob
     */
    public Boolean getCreateSonarJob() {
        return createSonarJob;
    }

    /**
     * @param createSonarJob
     *            the createSonarJob to set
     */
    public void setCreateSonarJob(Boolean createSonarJob) {
        this.createSonarJob = createSonarJob;
    }

    /**
     * @return the parameterMap
     */
    public Map<String, String> getParameterMap() {
        return parameterMap;
    }

    /**
     * @param parameterMap
     *            the parameterMap to set
     */
    public void setParameterMap(Map<String, String> parameterMap) {
        this.parameterMap = parameterMap;
    }

    public List<InputParameterModule> getModules() {
        return modules;
    }

    public void setModules(List<InputParameterModule> modules) {
        this.modules = modules;
    }

}
