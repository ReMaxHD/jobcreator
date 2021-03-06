/*
 * -----------------------------------------------------------------------------
 * Application     : WM 6
 * Revision        : $Revision: 376093 $
 * Revision date   : $Date: 2016-10-22 02:45:35 +0200 (Sa, 22 Okt 2016) $
 * Last changed by : $Author: b2yeowo $
 * URL             : $URL: http://almscdc.swisslog.com/repo/SWPD/Development/WM6/trunk/Software/WM6/wm6-tools/jenkins-jobcreator/src/test/java/com/swisslog/jenkins/jobcreator/polarion/workitem/WorkItemServiceTest.java $
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
package com.swisslog.jenkins.jobcreator.polarion.workitem;

import java.rmi.RemoteException;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.polarion.alm.ws.client.types.tracker.EnumOption;
import com.polarion.alm.ws.client.types.tracker.EnumOptionId;
import com.polarion.alm.ws.client.types.tracker.WorkItem;
import com.swisslog.jenkins.jobcreator.polarion.JobCreatorTestHelper;

public class WorkItemServiceTest {

    @Test
    public void test() throws WorkItemException, RemoteException {
        WorkItemServiceImpl workItemService = new WorkItemServiceImpl(
                JobCreatorTestHelper.createTrackerWebServiceMock());
        WorkItem workItem = JobCreatorTestHelper.createWorkItemMock();

        EnumOption enumOption = Mockito.mock(EnumOption.class);
        Mockito.when(enumOption.getName()).thenReturn(JobCreatorTestHelper.PROJECT_NAME);
        Mockito.when(
                workItemService.trackerService.getEnumOptionWithEnumId(Mockito.anyString(),
                        Mockito.matches("customer"), Mockito.any(EnumOptionId.class))).thenReturn(enumOption);

        Assert.assertEquals(JobCreatorTestHelper.PROJECT_NAME, workItemService.getProjectName(workItem));
        Assert.assertEquals(JobCreatorTestHelper.COUNTRY, workItemService.getCountry(workItem));
        Assert.assertEquals(JobCreatorTestHelper.WM6_VERSION, workItemService.getWm6Version(workItem));
        Assert.assertEquals(JobCreatorTestHelper.SVN_URL, workItemService.getSvnUrl(workItem));
        Assert.assertEquals(JobCreatorTestHelper.MAIL_RECIPIENTS, workItemService.getAuthorMail(workItem));
    }
}