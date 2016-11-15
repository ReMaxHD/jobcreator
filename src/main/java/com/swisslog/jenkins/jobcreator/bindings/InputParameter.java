package com.swisslog.jenkins.jobcreator.bindings;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * The class InputParameter is a simple binding to the config.xml
 */
@XmlRootElement(name = "inputparameter")
public class InputParameter {

    public static final String PROJECT_NAME = "%PROJECT_NAME%";
    public static final String LIST_NAME = "%LIST_NAME%";
    public static final String SLAVE = "%SLAVE%";
    public static final String SVN_URL = "%SVN_URL%";
    public static final String MAIL_RECIPIENTS = "%MAIL_RECIPIENTS%";
    public static final String ADMIN_MAIL_RECIPIENTS = "%ADMIN_MAIL_RECIPIENTS%";
    public static final String LOCK = "%LOCK%";
    public static final String LOCK_ST = "%LOCK_ST%";
    public static final String JDK_NAME = "%JDK_NAME%";
    public static final String MAIN_MVN_TARGETS = "%MAIN_MVN_TARGETS%";
    public static final String SONAR_BRANCH = "%SONAR_BRANCH%";
    public static final String JACOCO_ST_FOLDER = "%JACOCO_ST_FOLDER%";
    public static final String DATALOADER_CONFIGURATION = "%DATALOADER_CONFIGURATION%";
    public static final String MODULE_NAME_LOWERCASE = "%MODULE_NAME_LOWERCASE%";
    public static final String DOMAIN_NAME = "%DOMAIN_NAME%";
    public static final String SCREENSHOTS_DIR = "%SCREENSHOTS_DIR%";

    private Boolean abortOnError;

    private Map<String, String> parameterMap = new HashMap<String, String>();

    private String projectName;
    private String parentView;

    private Boolean createMainJob = false;
    private Boolean createSonarJob = false;
    private Boolean createWhitesourceJob = false;

    private Boolean createSystemTestJobs = false;

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

    public Boolean getCreateWhitesourceJob() {
        return createWhitesourceJob;
    }

    public void setCreateWhitesourceJob(Boolean createWhitesourceJob) {
        this.createWhitesourceJob = createWhitesourceJob;
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

}
