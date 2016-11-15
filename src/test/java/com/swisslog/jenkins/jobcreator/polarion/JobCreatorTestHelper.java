/*
 * -----------------------------------------------------------------------------
 * Application     : WM 6
 * Revision        : $Revision: 376093 $
 * Revision date   : $Date: 2016-10-22 02:45:35 +0200 (Sa, 22 Okt 2016) $
 * Last changed by : $Author: b2yeowo $
 * URL             : $URL: http://almscdc.swisslog.com/repo/SWPD/Development/WM6/trunk/Software/WM6/wm6-tools/jenkins-jobcreator/src/test/java/com/swisslog/jenkins/jobcreator/polarion/JobCreatorTestHelper.java $
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
package com.swisslog.jenkins.jobcreator.polarion;

import java.io.File;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.mockito.Mockito;

import com.polarion.alm.ws.client.tracker.TrackerWebService;
import com.polarion.alm.ws.client.types.projects.User;
import com.polarion.alm.ws.client.types.tracker.Custom;
import com.polarion.alm.ws.client.types.tracker.EnumOptionId;
import com.polarion.alm.ws.client.types.tracker.LinkedWorkItem;
import com.polarion.alm.ws.client.types.tracker.WorkItem;
import com.swisslog.jenkins.jobcreator.bindings.InputParameter;
import com.swisslog.jenkins.jobcreator.polarion.workitem.WorkItemException;
import com.swisslog.jenkins.jobcreator.polarion.workitem.WorkItemService;
import com.swisslog.jenkins.jobcreator.polarion.workitem.WorkItemServiceImpl;

public class JobCreatorTestHelper {

    public static final String INPUT_XML_LOCATION = "tests_tmp\\";
    public static final String XML_FILE_NAME = INPUT_XML_LOCATION + "input-polariontest-ch.xml";
    public static final String XML_FILE_NAME_ST = INPUT_XML_LOCATION + "input-polariontest-ch-st.xml";
    public static final File XML_FILE = new File(XML_FILE_NAME);

    public static final String PARENT_VIEW = "01 - Switzerland";
    public static final String PROJECT_NAME = "Polarion Test";
    public static final String INSTALLATION_SITE = "CH";
    public static final String COUNTRY = "ch";
    public static final String SLAVE = "Slave01";
    public static final String SVN_URL = "http://svn.swisslog.com/polarion test";
    public static final String MAIL_RECIPIENTS = "test.author@swisslog.com";
    public static final String ADMIN_MAIL_RECIPIENTS = "patrik.jetzer@swisslog.com pedro.oliveira@swisslog.com tim.eick@swisslog.com marc.lenicka@swisslog.com";
    public static final String LOCK = "Polarion Test - CH Build";
    public static final String JDK_NAME = "1.7";
    public static final String WM6_VERSION = "WM6.13.0-Beta1";
    public static final String MAIN_MVN_TARGETS = "clean install";
    public static final String JACOCO_ST_FOLDER = "polariontestch";
    public static final String SONAR_BRANCH = "PolarionTestCH";
    public static final String LOCK_ST = "Slave01 System Test";
    public static final String DATALOADER_CONFIGURATION = "PolarionTest-DevelopmentConfiguration.xml";
    public static final String MODULE_NAME_LOWERCASE = "polariontest";
    public static final String DOMAIN_NAME = "polariontestch";
    public static final String SCREENSHOTS_DIR = "polariontestch";

    public static final String PROJECT_WORK_ITEM_URI = "http://example.com/customerProjects/exampleWorkItem";

    private JobCreatorTestHelper() {
        super();
    }

    public static TrackerWebService createTrackerWebServiceMock() throws RemoteException {
        EnumOptionId country = Mockito.mock(EnumOptionId.class);
        Mockito.when(country.getId()).thenReturn(JobCreatorTestHelper.COUNTRY);

        EnumOptionId wm6Version = Mockito.mock(EnumOptionId.class);
        Mockito.when(wm6Version.getId()).thenReturn(JobCreatorTestHelper.WM6_VERSION);

        List<Custom> customFields = new ArrayList<>();
        customFields.add(new Custom(WorkItemServiceImpl.COUNTRY, country));
        customFields.add(new Custom(WorkItemServiceImpl.WM6_VERSION, wm6Version));

        WorkItem projectWorkItem = Mockito.mock(WorkItem.class);
        EnumOptionId type = Mockito.mock(EnumOptionId.class);
        Mockito.when(type.getId()).thenReturn(WorkItemServiceImpl.CUSTOMER_PROJECT_WORK_ITEM_TYPE);
        Mockito.when(projectWorkItem.getType()).thenReturn(type);
        Mockito.when(projectWorkItem.getCustomFields()).thenReturn(customFields.toArray(new Custom[0]));

        TrackerWebService service = Mockito.mock(TrackerWebService.class);
        Mockito.when(service.getWorkItemByUri(PROJECT_WORK_ITEM_URI)).thenReturn(projectWorkItem);

        return service;
    }

    public static WorkItemService createWorkItemServiceMock() throws WorkItemException, RemoteException {
        WorkItemService service = Mockito.mock(WorkItemService.class);
        Mockito.when(service.getProjectName(Mockito.any(WorkItem.class))).thenReturn(PROJECT_NAME);
        Mockito.when(service.getInstallationSite(Mockito.any(WorkItem.class))).thenReturn(INSTALLATION_SITE);
        Mockito.when(service.getCountry(Mockito.any(WorkItem.class))).thenReturn(COUNTRY);
        Mockito.when(service.getWm6Version(Mockito.any(WorkItem.class))).thenReturn(WM6_VERSION);
        Mockito.when(service.getSvnUrl(Mockito.any(WorkItem.class))).thenReturn(SVN_URL);
        Mockito.when(service.getAuthorMail(Mockito.any(WorkItem.class))).thenReturn(MAIL_RECIPIENTS);
        return service;
    }

    public static WorkItem createWorkItemMock() {
        WorkItem workItem = Mockito.mock(WorkItem.class);

        User user = Mockito.mock(User.class);
        Mockito.when(user.getEmail()).thenReturn(MAIL_RECIPIENTS);
        Mockito.when(workItem.getAuthor()).thenReturn(user);

        List<Custom> customFields = new ArrayList<>();

        EnumOptionId projectOption = Mockito.mock(EnumOptionId.class);
        Mockito.when(projectOption.getId()).thenReturn(PROJECT_NAME);
        customFields.add(new Custom(WorkItemServiceImpl.CUSTOMER_PROJECT, projectOption));

        customFields.add(new Custom(WorkItemServiceImpl.SVN_REPOSITORY, SVN_URL));
        customFields.add(new Custom(WorkItemServiceImpl.MAIL_RECIPIENTS, MAIL_RECIPIENTS));
        Mockito.when(workItem.getCustomFields()).thenReturn(customFields.toArray(new Custom[0]));

        // create linked work item mock (customer project work item)
        LinkedWorkItem projectWorkItem = Mockito.mock(LinkedWorkItem.class);
        Mockito.when(projectWorkItem.getWorkItemURI()).thenReturn(PROJECT_WORK_ITEM_URI);
        Mockito.when(workItem.getLinkedWorkItems()).thenReturn(new LinkedWorkItem[] { projectWorkItem });

        return workItem;
    }

    public static InputParameter createInputParameterForMainJob() {
        InputParameter inputParameter = new InputParameter();
        inputParameter.setParentView(PARENT_VIEW);
        inputParameter.setProjectName(PROJECT_NAME + " - " + INSTALLATION_SITE);
        inputParameter.setAbortOnError(false);
        inputParameter.setCreateMainJob(true);
        inputParameter.setCreateSonarJob(true);
        inputParameter.setCreateWhitesourceJob(true);
        inputParameter.setCreateSystemTestJobs(false);

        Map<String, String> params = new HashMap<>();
        params.put(InputParameter.PROJECT_NAME, PROJECT_NAME + " - " + INSTALLATION_SITE);
        params.put(InputParameter.LIST_NAME, PROJECT_NAME + " - " + INSTALLATION_SITE);
        params.put(InputParameter.SLAVE, SLAVE);
        params.put(InputParameter.SVN_URL, SVN_URL);
        params.put(InputParameter.MAIL_RECIPIENTS, MAIL_RECIPIENTS);
        params.put(InputParameter.ADMIN_MAIL_RECIPIENTS, ADMIN_MAIL_RECIPIENTS);
        params.put(InputParameter.LOCK, LOCK);
        params.put(InputParameter.JDK_NAME, JDK_NAME);
        params.put(InputParameter.MAIN_MVN_TARGETS, MAIN_MVN_TARGETS);
        params.put(InputParameter.JACOCO_ST_FOLDER, JACOCO_ST_FOLDER);
        params.put(InputParameter.SONAR_BRANCH, SONAR_BRANCH);
        params.put(InputParameter.MODULE_NAME_LOWERCASE, MODULE_NAME_LOWERCASE);
        inputParameter.setParameterMap(params);

        return inputParameter;
    }

    public static InputParameter createInputParameterForSystemTests() {
        InputParameter inputParameter = new InputParameter();
        inputParameter.setParentView(PARENT_VIEW);
        inputParameter.setProjectName(PROJECT_NAME + " - " + INSTALLATION_SITE);
        inputParameter.setAbortOnError(false);
        inputParameter.setCreateMainJob(false);
        inputParameter.setCreateSonarJob(false);
        inputParameter.setCreateWhitesourceJob(false);
        inputParameter.setCreateSystemTestJobs(true);

        Map<String, String> params = new HashMap<>();
        params.put(InputParameter.PROJECT_NAME, PROJECT_NAME + " - " + INSTALLATION_SITE);
        params.put(InputParameter.LIST_NAME, PROJECT_NAME + " - " + INSTALLATION_SITE);
        params.put(InputParameter.SLAVE, SLAVE);
        params.put(InputParameter.SVN_URL, SVN_URL);
        params.put(InputParameter.MAIL_RECIPIENTS, MAIL_RECIPIENTS);
        params.put(InputParameter.ADMIN_MAIL_RECIPIENTS, ADMIN_MAIL_RECIPIENTS);
        params.put(InputParameter.LOCK, LOCK);
        params.put(InputParameter.LOCK_ST, LOCK_ST);
        params.put(InputParameter.JDK_NAME, JDK_NAME);
        params.put(InputParameter.MAIN_MVN_TARGETS, MAIN_MVN_TARGETS);
        params.put(InputParameter.JACOCO_ST_FOLDER, JACOCO_ST_FOLDER);
        params.put(InputParameter.SONAR_BRANCH, SONAR_BRANCH);
        params.put(InputParameter.MODULE_NAME_LOWERCASE, MODULE_NAME_LOWERCASE);
        params.put(InputParameter.DATALOADER_CONFIGURATION, DATALOADER_CONFIGURATION);
        params.put(InputParameter.DOMAIN_NAME, DOMAIN_NAME);
        params.put(InputParameter.SCREENSHOTS_DIR, SCREENSHOTS_DIR);

        return inputParameter;
    }

    public static void assertInputParameterForMainJob(InputParameter inputParameter) {
        Assert.assertEquals(PARENT_VIEW, inputParameter.getParentView());
        Assert.assertEquals(PROJECT_NAME + " - " + INSTALLATION_SITE, inputParameter.getProjectName());
        Assert.assertFalse(inputParameter.getAbortOnError());
        Assert.assertTrue(inputParameter.getCreateMainJob());
        Assert.assertTrue(inputParameter.getCreateSonarJob());
        Assert.assertTrue(inputParameter.getCreateWhitesourceJob());
        Assert.assertFalse(inputParameter.getCreateSystemTestJobs());

        Map<String, String> params = inputParameter.getParameterMap();
        Assert.assertEquals(PROJECT_NAME + " - " + INSTALLATION_SITE, params.get(InputParameter.PROJECT_NAME));
        Assert.assertEquals(PROJECT_NAME + " - " + INSTALLATION_SITE, params.get(InputParameter.LIST_NAME));
        Assert.assertEquals(SLAVE, params.get(InputParameter.SLAVE));
        Assert.assertEquals(SVN_URL, params.get(InputParameter.SVN_URL));
        Assert.assertEquals(MAIL_RECIPIENTS, params.get(InputParameter.MAIL_RECIPIENTS));
        Assert.assertEquals(ADMIN_MAIL_RECIPIENTS, params.get(InputParameter.ADMIN_MAIL_RECIPIENTS));
        Assert.assertEquals(LOCK, params.get(InputParameter.LOCK));
        Assert.assertEquals(JDK_NAME, params.get(InputParameter.JDK_NAME));
        Assert.assertEquals(MAIN_MVN_TARGETS, params.get(InputParameter.MAIN_MVN_TARGETS));
        Assert.assertEquals(JACOCO_ST_FOLDER, params.get(InputParameter.JACOCO_ST_FOLDER));
        Assert.assertEquals(SONAR_BRANCH, params.get(InputParameter.SONAR_BRANCH));
        Assert.assertEquals(MODULE_NAME_LOWERCASE, params.get(InputParameter.MODULE_NAME_LOWERCASE));
    }

    public static void assertInputParameterForSystemTests(InputParameter inputParameter) {
        Assert.assertEquals(PARENT_VIEW, inputParameter.getParentView());
        Assert.assertEquals(PROJECT_NAME + " - " + INSTALLATION_SITE, inputParameter.getProjectName());
        Assert.assertFalse(inputParameter.getAbortOnError());
        Assert.assertFalse(inputParameter.getCreateMainJob());
        Assert.assertFalse(inputParameter.getCreateSonarJob());
        Assert.assertFalse(inputParameter.getCreateWhitesourceJob());
        Assert.assertTrue(inputParameter.getCreateSystemTestJobs());

        Map<String, String> params = inputParameter.getParameterMap();
        Assert.assertEquals(PROJECT_NAME + " - " + INSTALLATION_SITE, params.get(InputParameter.PROJECT_NAME));
        Assert.assertEquals(PROJECT_NAME + " - " + INSTALLATION_SITE, params.get(InputParameter.LIST_NAME));
        Assert.assertEquals(SLAVE, params.get(InputParameter.SLAVE));
        Assert.assertEquals(SVN_URL, params.get(InputParameter.SVN_URL));
        Assert.assertEquals(MAIL_RECIPIENTS, params.get(InputParameter.MAIL_RECIPIENTS));
        Assert.assertEquals(ADMIN_MAIL_RECIPIENTS, params.get(InputParameter.ADMIN_MAIL_RECIPIENTS));
        Assert.assertEquals(LOCK, params.get(InputParameter.LOCK));
        Assert.assertEquals(LOCK_ST, params.get(InputParameter.LOCK_ST));
        Assert.assertEquals(JDK_NAME, params.get(InputParameter.JDK_NAME));
        Assert.assertEquals(MAIN_MVN_TARGETS, params.get(InputParameter.MAIN_MVN_TARGETS));
        Assert.assertEquals(JACOCO_ST_FOLDER, params.get(InputParameter.JACOCO_ST_FOLDER));
        Assert.assertEquals(SONAR_BRANCH, params.get(InputParameter.SONAR_BRANCH));
        Assert.assertEquals(MODULE_NAME_LOWERCASE, params.get(InputParameter.MODULE_NAME_LOWERCASE));
        Assert.assertEquals(DATALOADER_CONFIGURATION, params.get(InputParameter.DATALOADER_CONFIGURATION));
        Assert.assertEquals(DOMAIN_NAME, params.get(InputParameter.DOMAIN_NAME));
        Assert.assertEquals(SCREENSHOTS_DIR, params.get(InputParameter.SCREENSHOTS_DIR));
    }
}