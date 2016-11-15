/*
 * -----------------------------------------------------------------------------
 * Application     : WM 6
 * Revision        : $Revision: 376093 $
 * Revision date   : $Date: 2016-10-22 02:45:35 +0200 (Sa, 22 Okt 2016) $
 * Last changed by : $Author: b2yeowo $
 * URL             : $URL: http://almscdc.swisslog.com/repo/SWPD/Development/WM6/trunk/Software/WM6/wm6-tools/jenkins-jobcreator/src/test/java/com/swisslog/jenkins/jobcreator/polarion/config/ConfigTest.java $
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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import com.polarion.alm.ws.client.types.tracker.WorkItem;
import com.swisslog.jenkins.jobcreator.polarion.JobCreatorTestHelper;
import com.swisslog.jenkins.jobcreator.polarion.workitem.WorkItemException;
import com.swisslog.jenkins.jobcreator.polarion.workitem.WorkItemService;

public class ConfigTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testLoadConfig() throws JAXBException, WorkItemException, RemoteException {
        Config config = Config.loadConfig(JobCreatorTestHelper.createWorkItemMock(),
                JobCreatorTestHelper.createWorkItemServiceMock());
        Assert.assertNotNull(config);

        Assert.assertNotNull(config.getSvnlookExe());
        Assert.assertFalse(config.getSvnlookExe().isEmpty());

        Assert.assertNotNull(config.getJobcreatorConfig());
        Assert.assertFalse(config.getJobcreatorConfig().isEmpty());

        Assert.assertNotNull(config.getInputXmlLocation());
        Assert.assertFalse(config.getInputXmlLocation().isEmpty());

        Assert.assertNotNull(config.getAdminMailRecipients());
        Assert.assertFalse(config.getAdminMailRecipients().isEmpty());

        Assert.assertNotNull(config.getMainMvnTargets());
        Assert.assertFalse(config.getMainMvnTargets().isEmpty());

        Assert.assertNotNull(config.getLockSufix());
        Assert.assertFalse(config.getLockSufix().isEmpty());

        Assert.assertNotNull(config.getDataLoaderConfigurationSufix());
        Assert.assertFalse(config.getDataLoaderConfigurationSufix().isEmpty());

        Assert.assertNotNull(config.stLocks);
        Assert.assertFalse(config.stLocks.isEmpty());

        Assert.assertNotNull(config.countries);
        Assert.assertFalse(config.countries.isEmpty());

        Assert.assertNotNull(config.wm6Versions);
        Assert.assertFalse(config.wm6Versions.isEmpty());
    }

    @Test
    public void testGetSystemTestLock() throws RemoteException, WorkItemException, JAXBException {
        WorkItem workItem = Mockito.mock(WorkItem.class);
        WorkItemService workItemService = Mockito.mock(WorkItemService.class);
        Mockito.when(workItemService.getCountry(workItem)).thenReturn(JobCreatorTestHelper.COUNTRY);

        Config config = Config.loadConfig(workItem, workItemService);

        // country is needed to get the slave
        Country country = new Country(JobCreatorTestHelper.COUNTRY, JobCreatorTestHelper.SLAVE,
                JobCreatorTestHelper.PARENT_VIEW);
        config.countries.clear();
        config.countries.add(country);

        // the system test lock depends on the slave
        Assert.assertEquals(JobCreatorTestHelper.SLAVE, config.getSlave());

        Assert.assertEquals(JobCreatorTestHelper.LOCK_ST, config.getSystemTestLock());
    }

    @Test
    public void testGetCountry() throws RemoteException, JAXBException, WorkItemException {
        WorkItem workItem = Mockito.mock(WorkItem.class);
        WorkItemService workItemService = Mockito.mock(WorkItemService.class);
        Config config = Config.loadConfig(workItem, workItemService);

        Mockito.when(workItemService.getCountry(workItem)).thenReturn(JobCreatorTestHelper.COUNTRY);
        Country country = new Country(JobCreatorTestHelper.COUNTRY, JobCreatorTestHelper.SLAVE,
                JobCreatorTestHelper.PARENT_VIEW);
        config.countries.clear();
        config.countries.add(country);
        Assert.assertEquals(country, config.getCountry());

        Mockito.when(workItemService.getCountry(workItem)).thenReturn("ThisCountryDoesNotExist");
        thrown.expect(IllegalStateException.class);
        config.getCountry();
    }

    @Test
    public void testGetJdkName() throws RemoteException, WorkItemException, JAXBException {
        WorkItem workItem = Mockito.mock(WorkItem.class);
        WorkItemService workItemService = Mockito.mock(WorkItemService.class);
        Mockito.when(workItemService.getWm6Version(workItem)).thenReturn(JobCreatorTestHelper.WM6_VERSION);

        Config config = Config.loadConfig(workItem, workItemService);
        // remove all versions which were loaded from config file
        config.wm6Versions.clear();

        // weak match (with x)
        config.wm6Versions.add(new Wm6Version("WM6.x", "1.6"));
        Assert.assertEquals("1.6", config.getJdkName());

        // exact match (should overwrite weak match)
        config.wm6Versions.add(new Wm6Version(JobCreatorTestHelper.WM6_VERSION, "1.7"));
        Assert.assertEquals("1.7", config.getJdkName());

        // version doesn't exist
        thrown.expect(IllegalStateException.class);
        Mockito.when(workItemService.getWm6Version(workItem)).thenReturn("7.0.0");
        config.getJdkName();
    }
}