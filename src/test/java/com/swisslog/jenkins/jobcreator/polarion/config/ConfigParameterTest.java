/*
 * -----------------------------------------------------------------------------
 * Application     : WM 6
 * Revision        : $Revision: 376093 $
 * Revision date   : $Date: 2016-10-22 02:45:35 +0200 (Sa, 22 Okt 2016) $
 * Last changed by : $Author: b2yeowo $
 * URL             : $URL: http://almscdc.swisslog.com/repo/SWPD/Development/WM6/trunk/Software/WM6/wm6-tools/jenkins-jobcreator/src/test/java/com/swisslog/jenkins/jobcreator/polarion/config/ConfigParameterTest.java $
 *
 * -----------------------------------------------------------------------------
 * Copyright
 * This software module including the design and software principals used
 * is and remains the property of Swisslog and is submitted with the
 * understanding that it is not to be reproduced nor copied in whole or in
 * part, nor licensed or otherwise provided or communicated to any third
 * party without Swisslog's prior written consent.
 * It must not be used in any way detrimental to the interests of Swisslog.
 * Acceptance of this module will be construed as an agreement to the above.
 *
 * All rights of Swisslog remain reserved. Swisslog and WarehouseManager
 * are trademarks or registered trademarks of Swisslog. Other products
 * and company names mentioned herein may be trademarks or trade names of
 * their respective owners. Specifications are subject to change without
 * notice.
 * -----------------------------------------------------------------------------
 */
package com.swisslog.jenkins.jobcreator.polarion.config;

import java.rmi.RemoteException;

import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.polarion.alm.ws.client.types.tracker.WorkItem;
import com.swisslog.jenkins.jobcreator.polarion.workitem.WorkItemException;
import com.swisslog.jenkins.jobcreator.polarion.workitem.WorkItemService;

public class ConfigParameterTest {

    private static final String PROJECT_NAME = "Test Project";
    private static final String PROJECT_NAME_WITHOUT_SPACES = "TestProject";
    private static final String PROJECT_NAME_LOWER_CASE = "testproject";
    private static final String PROJECT_NAME_UNDERSCORE = "Test_Project";
    private static final String BUILD_LOCATION = "D:\\jenkins\\workspace\\Test_Project_-_Build";
    private static final String WAR = "wm6-testproject-war";
    private static final String INSTALLATION_SITE = "CH";
    private static final String FULL_NAME = "Test Project - CH";
    private static final String SONAR_BRANCH = "TestProjectCH";
    private static final String SONAR_BRANCH_WITHOUT_INSTALLATION_SITE = "TestProject";
    private static final String FULL_NAME_LOWER_CASE = "testprojectch";

    private WorkItem workItem;
    private WorkItemService workItemService;
    private Config config;

    @Before
    public void before() throws RemoteException, JAXBException, WorkItemException {
        workItem = Mockito.mock(WorkItem.class);
        workItemService = Mockito.mock(WorkItemService.class);
        Mockito.when(workItemService.getProjectName(workItem)).thenReturn(PROJECT_NAME);
        Mockito.when(workItemService.getInstallationSite(workItem)).thenReturn(INSTALLATION_SITE);
        config = Mockito.mock(Config.class);
        Mockito.when(config.getWorkItem()).thenReturn(workItem);
        Mockito.when(config.getWorkItemService()).thenReturn(workItemService);
    }

    @Test
    public void testProjectName() throws WorkItemException {
        String projectName = ConfigParameter.PROJECT_NAME.getValue(config);
        Assert.assertEquals(PROJECT_NAME, projectName);
    }

    @Test
    public void testProjectNameWithoutSpaces() throws WorkItemException {
        String projectName = ConfigParameter.PROJECT_NAME_WITHOUT_SPACES.getValue(config);
        Assert.assertEquals(PROJECT_NAME_WITHOUT_SPACES, projectName);
    }

    @Test
    public void testProjectNameLowerCase() throws WorkItemException {
        String projectName = ConfigParameter.PROJECT_NAME_LOWER_CASE.getValue(config);
        Assert.assertEquals(PROJECT_NAME_LOWER_CASE, projectName);
    }

    @Test
    public void testProjectNameUnderscore() throws WorkItemException {
        String projectName = ConfigParameter.PROJECT_NAME_UNDERSCORE.getValue(config);
        Assert.assertEquals(PROJECT_NAME_UNDERSCORE, projectName);
    }

    @Test
    public void testBuildLocation() throws WorkItemException {
        String projectName = ConfigParameter.BUILD_LOCATION.getValue(config);
        Assert.assertEquals(BUILD_LOCATION, projectName);
    }

    @Test
    public void testWar() throws WorkItemException {
        String projectName = ConfigParameter.WAR.getValue(config);
        Assert.assertEquals(WAR, projectName);
    }

    @Test
    public void testInstallationSite() throws WorkItemException {
        String installationSite = ConfigParameter.INSTALLATION_SITE.getValue(config);
        Assert.assertEquals(INSTALLATION_SITE, installationSite);
    }

    @Test
    public void testFullName() throws WorkItemException {
        String fullName = ConfigParameter.FULL_NAME.getValue(config);
        Assert.assertEquals(FULL_NAME, fullName);
    }

    @Test
    public void testFullName_withoutInstallationSite() throws WorkItemException, RemoteException {
        Mockito.when(workItemService.getInstallationSite(workItem)).thenReturn(null);
        String fullName = ConfigParameter.FULL_NAME.getValue(config);
        Assert.assertEquals(PROJECT_NAME, fullName);

        Mockito.when(workItemService.getInstallationSite(workItem)).thenReturn("");
        fullName = ConfigParameter.FULL_NAME.getValue(config);
        Assert.assertEquals(PROJECT_NAME, fullName);
    }

    @Test
    public void testSonarBranch() throws WorkItemException {
        String sonarBranch = ConfigParameter.SONAR_BRANCH.getValue(config);
        Assert.assertEquals(SONAR_BRANCH, sonarBranch);
    }

    @Test
    public void testSonarBranch_withoutInstallationSite() throws WorkItemException, RemoteException {
        Mockito.when(workItemService.getInstallationSite(workItem)).thenReturn(null);
        String sonarBranch = ConfigParameter.SONAR_BRANCH.getValue(config);
        Assert.assertEquals(SONAR_BRANCH_WITHOUT_INSTALLATION_SITE, sonarBranch);

        Mockito.when(workItemService.getInstallationSite(workItem)).thenReturn("");
        sonarBranch = ConfigParameter.SONAR_BRANCH.getValue(config);
        Assert.assertEquals(SONAR_BRANCH_WITHOUT_INSTALLATION_SITE, sonarBranch);
    }

    @Test
    public void testFullNameLowerCase() throws WorkItemException {
        String fullNameLowerCase = ConfigParameter.FULL_NAME_LOWER_CASE.getValue(config);
        Assert.assertEquals(FULL_NAME_LOWER_CASE, fullNameLowerCase);
    }

    @Test
    public void testFullNameLowerCase_withoutInstallationSite() throws WorkItemException, RemoteException {
        Mockito.when(workItemService.getInstallationSite(workItem)).thenReturn(null);
        String fullNameLowerCase = ConfigParameter.FULL_NAME_LOWER_CASE.getValue(config);
        Assert.assertEquals(PROJECT_NAME_LOWER_CASE, fullNameLowerCase);

        Mockito.when(workItemService.getInstallationSite(workItem)).thenReturn("");
        fullNameLowerCase = ConfigParameter.FULL_NAME_LOWER_CASE.getValue(config);
        Assert.assertEquals(PROJECT_NAME_LOWER_CASE, fullNameLowerCase);
    }
}