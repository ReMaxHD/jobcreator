package com.swisslog.jenkins.jobcreator.bindings;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The Class Config is a simple binding to the config.xml
 */
@XmlRootElement(name = "config")
@XmlAccessorType(XmlAccessType.FIELD)
public class Config {

    private String templatesDirectory;

    private String hostScheme;
    private String hostName;
    private Integer hostPort;

    private String hostUsername;
    private String hostPassword;

    private String mainJobSuffix;
    private String cleanupJobSuffix;
    private String integrationTestHsqlSuffix;
    private String integrationTestOracleSuffix;
    private String integrationTestSqlSuffix;
    private String sonarJobSuffix;
    private String whitesourceJobSuffix;

    private String stBuildJobSuffix;
    private String stDataloaderJobSuffix;
    private String stStartServerJobSuffix;
    private String stDeployJobSuffix;
    private String stTestsJobSuffix;
    private String stUITestsJobSuffix;
    private String stUndeployJobSuffix;
    private String stStopServerJobSuffix;
    private String stMultijobSequenceJobSuffix;

    @XmlElement(name = "jdkInstallation")
    private List<JdkInstallation> jdkInstallations;

    /**
     * @return the templatesDirectory
     */
    public String getTemplatesDirectory() {
        return templatesDirectory;
    }

    /**
     * @param templatesDirectory
     *            the templatesDirectory to set
     */
    public void setTemplatesDirectory(String templatesDirectory) {
        this.templatesDirectory = templatesDirectory;
    }

    /**
     * @return the hostScheme
     */
    public String getHostScheme() {
        return hostScheme;
    }

    /**
     * @param hostScheme
     *            the hostScheme to set
     */
    public void setHostScheme(String hostScheme) {
        this.hostScheme = hostScheme;
    }

    /**
     * @return the hostName
     */
    public String getHostName() {
        return hostName;
    }

    /**
     * @param hostName
     *            the hostName to set
     */
    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    /**
     * @return the hostPort
     */
    public Integer getHostPort() {
        return hostPort;
    }

    /**
     * @param hostPort
     *            the hostPort to set
     */
    public void setHostPort(Integer hostPort) {
        this.hostPort = hostPort;
    }

    /**
     * @return the hostUsername
     */
    public String getHostUsername() {
        return hostUsername;
    }

    /**
     * @param hostUsername
     *            the hostUsername to set
     */
    public void setHostUsername(String hostUsername) {
        this.hostUsername = hostUsername;
    }

    /**
     * @return the hostPassword
     */
    public String getHostPassword() {
        return hostPassword;
    }

    /**
     * @param hostPassword
     *            the hostPassword to set
     */
    public void setHostPassword(String hostPassword) {
        this.hostPassword = hostPassword;
    }

    /**
     * @return the mainJobSuffix
     */
    public String getMainJobSuffix() {
        return mainJobSuffix;
    }

    /**
     * @param mainJobSuffix
     *            the mainJobSuffix to set
     */
    public void setMainJobSuffix(String mainJobSuffix) {
        this.mainJobSuffix = mainJobSuffix;
    }

    public String getCleanupJobSuffix() {
        return cleanupJobSuffix;
    }

    public void setCleanupJobSuffix(String cleanupJobSuffix) {
        this.cleanupJobSuffix = cleanupJobSuffix;
    }

    public String getIntegrationTestHsqlSuffix() {
        return integrationTestHsqlSuffix;
    }

    public void setIntegrationTestHsqlSuffix(String integrationTestHsqlSuffix) {
        this.integrationTestHsqlSuffix = integrationTestHsqlSuffix;
    }

    public String getIntegrationTestOracleSuffix() {
        return integrationTestOracleSuffix;
    }

    public void setIntegrationTestOracleSuffix(String integrationTestOracleSuffix) {
        this.integrationTestOracleSuffix = integrationTestOracleSuffix;
    }

    public String getIntegrationTestSqlSuffix() {
        return integrationTestSqlSuffix;
    }

    public void setIntegrationTestSqlSuffix(String integrationTestSqlSuffix) {
        this.integrationTestSqlSuffix = integrationTestSqlSuffix;
    }

    /**
     * @return the sonarJobSuffix
     */
    public String getSonarJobSuffix() {
        return sonarJobSuffix;
    }

    /**
     * @param sonarJobSuffix
     *            the sonarJobSuffix to set
     */
    public void setSonarJobSuffix(String sonarJobSuffix) {
        this.sonarJobSuffix = sonarJobSuffix;
    }

    public String getWhitesourceJobSuffix() {
        return whitesourceJobSuffix;
    }

    public void setWhitesourceJobSuffix(String whitesourceJobSuffix) {
        this.whitesourceJobSuffix = whitesourceJobSuffix;
    }

    /**
     * @return the stBuildJobSuffix
     */
    public String getStBuildJobSuffix() {
        return stBuildJobSuffix;
    }

    /**
     * @param stBuildJobSuffix
     *            the stBuildJobSuffix to set
     */
    public void setStBuildJobSuffix(String stBuildJobSuffix) {
        this.stBuildJobSuffix = stBuildJobSuffix;
    }

    /**
     * @return the stDataloaderJobSuffix
     */
    public String getStDataloaderJobSuffix() {
        return stDataloaderJobSuffix;
    }

    /**
     * @param stDataloaderJobSuffix
     *            the stDataloaderJobSuffix to set
     */
    public void setStDataloaderJobSuffix(String stDataloaderJobSuffix) {
        this.stDataloaderJobSuffix = stDataloaderJobSuffix;
    }

    /**
     * @return the stStartServerJobSuffix
     */
    public String getStStartServerJobSuffix() {
        return stStartServerJobSuffix;
    }

    /**
     * @param stStartServerJobSuffix
     *            the stStartServerJobSuffix to set
     */
    public void setStStartServerJobSuffix(String stStartServerJobSuffix) {
        this.stStartServerJobSuffix = stStartServerJobSuffix;
    }

    /**
     * @return the stDeployJobSuffix
     */
    public String getStDeployJobSuffix() {
        return stDeployJobSuffix;
    }

    /**
     * @param stDeployJobSuffix
     *            the stDeployJobSuffix to set
     */
    public void setStDeployJobSuffix(String stDeployJobSuffix) {
        this.stDeployJobSuffix = stDeployJobSuffix;
    }

    /**
     * @return the stTestsJobSuffix
     */
    public String getStTestsJobSuffix() {
        return stTestsJobSuffix;
    }

    /**
     * @param stTestsJobSuffix
     *            the stTestsJobSuffix to set
     */
    public void setStTestsJobSuffix(String stTestsJobSuffix) {
        this.stTestsJobSuffix = stTestsJobSuffix;
    }

    public String getStUITestsJobSuffix() {
        return stUITestsJobSuffix;
    }

    public void setStUITestsJobSuffix(String stUITestsJobSuffix) {
        this.stUITestsJobSuffix = stUITestsJobSuffix;
    }

    /**
     * @return the stUndeployJobSuffix
     */
    public String getStUndeployJobSuffix() {
        return stUndeployJobSuffix;
    }

    /**
     * @param stUndeployJobSuffix
     *            the stUndeployJobSuffix to set
     */
    public void setStUndeployJobSuffix(String stUndeployJobSuffix) {
        this.stUndeployJobSuffix = stUndeployJobSuffix;
    }

    /**
     * @return the stStopServerJobSuffix
     */
    public String getStStopServerJobSuffix() {
        return stStopServerJobSuffix;
    }

    /**
     * @param stStopServerJobSuffix
     *            the stStopServerJobSuffix to set
     */
    public void setStStopServerJobSuffix(String stStopServerJobSuffix) {
        this.stStopServerJobSuffix = stStopServerJobSuffix;
    }

    /**
     * @return the stMultijobSequenceJobSuffix
     */
    public String getStMultijobSequenceJobSuffix() {
        return stMultijobSequenceJobSuffix;
    }

    /**
     * @param stMultijobSequenceJobSuffix
     *            the stMultijobSequenceJobSuffix to set
     */
    public void setStMultijobSequenceJobSuffix(String stMultijobSequenceJobSuffix) {
        this.stMultijobSequenceJobSuffix = stMultijobSequenceJobSuffix;
    }

    /**
     * Get the jdk installations
     *
     * @return the jdk installations
     */
    public List<JdkInstallation> getJdkInstallations() {
        return jdkInstallations;
    }

    /**
     * Set the jdk installations
     *
     * @param jdkInstallations
     *            the jdk installations
     */
    public void setJdkInstallations(List<JdkInstallation> jdkInstallations) {
        this.jdkInstallations = jdkInstallations;
    }

}
