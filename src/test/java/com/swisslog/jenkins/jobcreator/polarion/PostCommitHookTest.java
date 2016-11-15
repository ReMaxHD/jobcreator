/*
 * -----------------------------------------------------------------------------
 * Application     : WM 6
 * Revision        : $Revision: 376093 $
 * Revision date   : $Date: 2016-10-22 02:45:35 +0200 (Sa, 22 Okt 2016) $
 * Last changed by : $Author: b2yeowo $
 * URL             : $URL: http://almscdc.swisslog.com/repo/SWPD/Development/WM6/trunk/Software/WM6/wm6-tools/jenkins-jobcreator/src/test/java/com/swisslog/jenkins/jobcreator/polarion/PostCommitHookTest.java $
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

import java.rmi.RemoteException;

import javax.xml.rpc.ServiceException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.polarion.alm.ws.client.WebServiceFactory;
import com.polarion.alm.ws.client.session.SessionWebService;
import com.polarion.alm.ws.client.tracker.TrackerWebService;
import com.polarion.alm.ws.client.types.tracker.EnumOptionId;
import com.polarion.alm.ws.client.types.tracker.WorkItem;

public class PostCommitHookTest {

    public static final String WORK_ITEM_ID = "TRK-112";
    public static final String REPO_PATH = "http:\\test.test\\example\\workitems\\" + WORK_ITEM_ID + "\\workitem.xml";
    public static final String REVISION = "12";

    private static final String POLARION_USER = "test";
    private static final String POLARION_PASSWORD = "1234";
    private static final String POLARION_WEBSERVICE_URL = "http://test.com/polarion/";

    private static final String ENVIRONMENT_PROPERTIES = "env/environment-unittest.properties";

    private PostCommitHook postCommitHook;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void before() throws Exception {
        WebServiceFactory webServiceFactory = Mockito.mock(WebServiceFactory.class);
        SessionWebService sessionService = Mockito.mock(SessionWebService.class);
        TrackerWebService trackerService = Mockito.mock(TrackerWebService.class);
        Mockito.when(webServiceFactory.getSessionService()).thenReturn(sessionService);
        Mockito.when(webServiceFactory.getTrackerService()).thenReturn(trackerService);
        postCommitHook = new PostCommitHook(REPO_PATH, REVISION, webServiceFactory);
        postCommitHook.trackerService = trackerService;
        postCommitHook.sessionService = sessionService;
        postCommitHook.jobCreatorService = Mockito.mock(JobCreatorService.class);
    }

    @Test
    public void testLoadEnvironment() {
        PostCommitHook.loadEnvironment(ENVIRONMENT_PROPERTIES);
        Assert.assertEquals(POLARION_USER, PostCommitHook.polarionUser);
        Assert.assertEquals(POLARION_PASSWORD, PostCommitHook.polarionPassword);
        Assert.assertEquals(POLARION_WEBSERVICE_URL, PostCommitHook.polarionWebserviceUrl);
    }

    @Test(timeout = PostCommitHook.TIMEOUT)
    public void testRunWithTimeout() {
        PostCommitHook hookSpy = Mockito.spy(postCommitHook);
        Mockito.doReturn(true).when(hookSpy).run();
        Assert.assertTrue(hookSpy.runWithTimeout());
    }

    // ignore this test because it takes too long
    @Ignore
    @Test
    public void testRunWithTimeoutExpired() {
        PostCommitHook hookSpy = Mockito.spy(postCommitHook);
        Mockito.doAnswer(new Answer<Boolean>() {

            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                Thread.sleep(PostCommitHook.TIMEOUT + 2000);
                return true;
            }

        }).when(hookSpy).run();
        Assert.assertFalse(hookSpy.runWithTimeout());
    }

    @Test
    public void testSetUpSession() throws RemoteException, ServiceException {
        SessionWebService sessionService = Mockito.mock(SessionWebService.class);
        Mockito.when(postCommitHook.factory.getSessionService()).thenReturn(sessionService);

        postCommitHook.setUpSession();
        Mockito.verify(sessionService).logIn(PostCommitHook.polarionUser, PostCommitHook.polarionPassword);
    }

    @Test
    public void testGetWorkItemIdFromLine() {
        String line = "U   ProductDevelopment/Tracker/.polarion/tracker/workitems/10000-19999/11000-11999/11200-11299/TRK-11259/workitem.xml";
        String workItemId = postCommitHook.getWorkItemIdFromLine(line);
        Assert.assertEquals("TRK-11259", workItemId);
    }

    @Test
    public void testGetWorkItem() throws RemoteException {
        WorkItem[] workItems = new WorkItem[] { JobCreatorTestHelper.createWorkItemMock() };
        Mockito.when(workItems[0].getStatus()).thenReturn(Mockito.mock(EnumOptionId.class));
        Mockito.when(
                postCommitHook.trackerService.queryWorkItems("(project.id:ProductTracker AND id:TRK-112)", null,
                        new String[] { "id" })).thenReturn(workItems);
        Assert.assertEquals(workItems[0], postCommitHook.getWorkItem(WORK_ITEM_ID));
    }

    @Test
    public void testGetWorkItem_statusIsNull() throws RemoteException {
        WorkItem[] workItems = new WorkItem[] { JobCreatorTestHelper.createWorkItemMock() };
        Mockito.when(
                postCommitHook.trackerService.queryWorkItems("(project.id:ProductTracker AND id:TRK-112)", null,
                        new String[] { "id" })).thenReturn(workItems);

        WorkItem workItem = Mockito.mock(WorkItem.class);
        Mockito.when(workItem.getStatus()).thenReturn(Mockito.mock(EnumOptionId.class));
        Mockito.when(
                postCommitHook.trackerService.getWorkItemByUriInRevision(Mockito.anyString(), Mockito.matches(REVISION)))
                .thenReturn(workItem);

        Assert.assertEquals(workItem, postCommitHook.getWorkItem(WORK_ITEM_ID));
    }

    @Test
    public void testGetWorkItem_statusIsStillNull() throws RemoteException {
        WorkItem[] workItems = new WorkItem[] { JobCreatorTestHelper.createWorkItemMock() };
        Mockito.when(
                postCommitHook.trackerService.queryWorkItems("(project.id:ProductTracker AND id:TRK-112)", null,
                        new String[] { "id" })).thenReturn(workItems);

        WorkItem workItem = Mockito.mock(WorkItem.class);
        Mockito.when(
                postCommitHook.trackerService.getWorkItemByUriInRevision(Mockito.anyString(), Mockito.matches(REVISION)))
                .thenReturn(workItem);

        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Status of work item is null");
        postCommitHook.getWorkItem(WORK_ITEM_ID);
    }

    @Test
    public void testGetWorkItemNotFound() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Could not find work item with id " + WORK_ITEM_ID);

        postCommitHook.getWorkItem(WORK_ITEM_ID);
    }

    @Test
    public void testGetWorkItemUnresolvable() throws RemoteException {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Work item " + WORK_ITEM_ID + " is unresolvable");

        WorkItem[] workItems = new WorkItem[] { JobCreatorTestHelper.createWorkItemMock() };
        Mockito.when(workItems[0].isUnresolvable()).thenReturn(true);
        Mockito.when(
                postCommitHook.trackerService.queryWorkItems("(project.id:ProductTracker AND id:TRK-112)", null,
                        new String[] { "id" })).thenReturn(workItems);
        postCommitHook.getWorkItem(WORK_ITEM_ID);
    }

    @Test
    public void testCreateJobs_centralTools() throws RemoteException, ServiceException {
        WorkItem workItem = createWorkItem(PostCommitHook.STATUS, PostCommitHook.CENTRAL_TOOLS);
        postCommitHook.createJobs(workItem);

        Mockito.verify(postCommitHook.jobCreatorService).createMainJobs(workItem);
        Mockito.verify(postCommitHook.jobCreatorService, Mockito.never()).createSystemTestJobs(
                Mockito.any(WorkItem.class));
    }

    @Test
    public void testCreateJobs_systemTests() throws RemoteException, ServiceException {
        WorkItem workItem = createWorkItem(PostCommitHook.STATUS, PostCommitHook.SYSTEM_TESTS);
        postCommitHook.createJobs(workItem);

        Mockito.verify(postCommitHook.jobCreatorService, Mockito.never()).createMainJobs(Mockito.any(WorkItem.class));
        Mockito.verify(postCommitHook.jobCreatorService).createSystemTestJobs(workItem);
    }

    @Test
    public void testCreateJobs_wrongStatus() throws RemoteException, ServiceException {
        WorkItem workItem = createWorkItem("This status is wrong", PostCommitHook.CENTRAL_TOOLS);
        postCommitHook.createJobs(workItem);

        Mockito.verify(postCommitHook.jobCreatorService, Mockito.never()).createMainJobs(Mockito.any(WorkItem.class));
        Mockito.verify(postCommitHook.jobCreatorService, Mockito.never()).createSystemTestJobs(
                Mockito.any(WorkItem.class));
    }

    @Test
    public void testCreateJobs_unknownType() throws RemoteException, ServiceException {
        WorkItem workItem = createWorkItem(PostCommitHook.STATUS, "UnknownType");
        postCommitHook.createJobs(workItem);

        Mockito.verify(postCommitHook.jobCreatorService, Mockito.never()).createMainJobs(Mockito.any(WorkItem.class));
        Mockito.verify(postCommitHook.jobCreatorService, Mockito.never()).createSystemTestJobs(
                Mockito.any(WorkItem.class));
    }

    private WorkItem createWorkItem(String status, String type) {
        WorkItem workItem = JobCreatorTestHelper.createWorkItemMock();

        EnumOptionId statusOption = Mockito.mock(EnumOptionId.class);
        Mockito.when(statusOption.getId()).thenReturn(status);
        Mockito.when(workItem.getStatus()).thenReturn(statusOption);

        EnumOptionId typeOption = Mockito.mock(EnumOptionId.class);
        Mockito.when(typeOption.getId()).thenReturn(type);
        Mockito.when(workItem.getType()).thenReturn(typeOption);

        return workItem;
    }
}