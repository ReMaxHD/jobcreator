/*
 * -----------------------------------------------------------------------------
 * Application     : WM 6
 * Revision        : $Revision: 376093 $
 * Revision date   : $Date: 2016-10-22 02:45:35 +0200 (Sa, 22 Okt 2016) $
 * Last changed by : $Author: b2yeowo $
 * URL             : $URL: http://almscdc.swisslog.com/repo/SWPD/Development/WM6/trunk/Software/WM6/wm6-tools/jenkins-jobcreator/src/test/java/com/swisslog/jenkins/jobcreator/polarion/JobCreatorServiceTest.java $
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

import static com.swisslog.jenkins.jobcreator.polarion.JobCreatorTestHelper.INPUT_XML_LOCATION;
import static com.swisslog.jenkins.jobcreator.polarion.JobCreatorTestHelper.XML_FILE;
import static com.swisslog.jenkins.jobcreator.polarion.JobCreatorTestHelper.XML_FILE_NAME;
import static com.swisslog.jenkins.jobcreator.polarion.JobCreatorTestHelper.XML_FILE_NAME_ST;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import com.polarion.alm.ws.client.types.tracker.WorkItem;
import com.swisslog.jenkins.jobcreator.bindings.InputParameter;
import com.swisslog.jenkins.jobcreator.polarion.config.Config;
import com.swisslog.jenkins.jobcreator.polarion.launcher.JobCreatorLauncher;
import com.swisslog.jenkins.jobcreator.polarion.mail.MailService;
import com.swisslog.jenkins.jobcreator.polarion.workitem.WorkItemException;

public class JobCreatorServiceTest {

    private JobCreatorService service;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void before() throws WorkItemException, RemoteException {
        service = new JobCreatorService(JobCreatorTestHelper.createTrackerWebServiceMock());
        service.mailService = Mockito.mock(MailService.class);
        service.launcher = Mockito.mock(JobCreatorLauncher.class);
        service.workItemService = JobCreatorTestHelper.createWorkItemServiceMock();

        File testFolder = new File(INPUT_XML_LOCATION);
        if (!testFolder.exists()) {
            testFolder.mkdir();
        }
        if (XML_FILE.exists()) {
            boolean deleted = XML_FILE.delete();
            Assert.assertTrue("Could not delete test file", deleted);
        }
    }

    @Test
    public void testCreateInputParameterForMainJob() throws JAXBException, WorkItemException, RemoteException {
        WorkItem workItem = JobCreatorTestHelper.createWorkItemMock();
        Config config = Config.loadConfig(workItem, service.workItemService);
        InputParameter inputParameter = service.createInputParameterForMainJobs(workItem, config);
        JobCreatorTestHelper.assertInputParameterForMainJob(inputParameter);
    }

    @Test
    public void testCreateInputParameterForMainJobWithoutMailRecipients() throws RemoteException, JAXBException,
    WorkItemException {
        WorkItem workItem = JobCreatorTestHelper.createWorkItemMock();
        Config config = Config.loadConfig(workItem, service.workItemService);

        InputParameter inputParameterWithoutRecipients = service.createInputParameterForMainJobs(workItem, config);
        Assert.assertEquals(JobCreatorTestHelper.MAIL_RECIPIENTS, inputParameterWithoutRecipients.getParameterMap()
                .get(InputParameter.MAIL_RECIPIENTS));
    }

    @Test
    public void testCreateFileName() throws JAXBException, WorkItemException, RemoteException {
        Config config = Config.loadConfig(null, service.workItemService);
        config.setInputXmlLocation(INPUT_XML_LOCATION);
        String fileName = service.createFileName(null, config, "");
        Assert.assertEquals(XML_FILE_NAME, fileName);
    }

    @Test
    public void testCreateInputParameterForSystemTestJobs() throws JAXBException, WorkItemException, RemoteException {
        Config config = Config.loadConfig(null, service.workItemService);
        InputParameter inputParameter = service.createInputParameterForSystemTestJobs(null, config);
        JobCreatorTestHelper.assertInputParameterForSystemTests(inputParameter);
    }

    @Test
    public void testCreateInputXml() throws IOException, JAXBException {
        InputParameter inputParameter = JobCreatorTestHelper.createInputParameterForMainJob();

        // create xml
        service.createInputXml(XML_FILE_NAME, inputParameter, false);
        Assert.assertTrue(XML_FILE.exists());

        // check created xml
        Unmarshaller unmarshaller = JAXBContext.newInstance(InputParameter.class).createUnmarshaller();
        InputParameter result = unmarshaller.unmarshal(new StreamSource(XML_FILE), InputParameter.class).getValue();
        JobCreatorTestHelper.assertInputParameterForMainJob(result);
    }

    @Test
    public void testCreateInputXmlOverwriteException() throws IOException, JAXBException {
        thrown.expect(IOException.class);
        thrown.expectMessage("Could not create file " + XML_FILE_NAME + " because it already exists");

        InputParameter inputParameter = JobCreatorTestHelper.createInputParameterForMainJob();
        Assert.assertTrue("Could not create input xml", XML_FILE.createNewFile());

        service.createInputXml(XML_FILE_NAME, inputParameter, false);
    }

    @Test
    public void testCreateInputXmlOverwrite() throws IOException, JAXBException {
        InputParameter inputParameter = JobCreatorTestHelper.createInputParameterForMainJob();
        Assert.assertTrue("Could not create input xml", XML_FILE.createNewFile());

        // overwrite it
        String overwrittenParentView = "test view 2";
        inputParameter.setParentView(overwrittenParentView);
        service.createInputXml(XML_FILE_NAME, inputParameter, true);
        Assert.assertTrue(XML_FILE.exists());

        // check overwritten xml
        Unmarshaller unmarshaller = JAXBContext.newInstance(InputParameter.class).createUnmarshaller();
        InputParameter result = unmarshaller.unmarshal(new StreamSource(XML_FILE), InputParameter.class).getValue();
        Assert.assertEquals(overwrittenParentView, result.getParentView());
    }

    @Test
    public void testGetInputParameter() throws JAXBException, IOException {
        // create xml file
        if (!XML_FILE.exists()) {
            InputParameter inputParameter = JobCreatorTestHelper.createInputParameterForMainJob();
            service.createInputXml(XML_FILE_NAME, inputParameter, false);
        }
        Assert.assertTrue(XML_FILE.exists());

        // test getInputParameter
        InputParameter inputParameter = service.getInputParameter(XML_FILE_NAME);
        JobCreatorTestHelper.assertInputParameterForMainJob(inputParameter);
    }

    @Test
    public void testGetInputParameterExceptin() throws JAXBException {
        thrown.expect(UnmarshalException.class);

        // delete xml file
        if (XML_FILE.exists()) {
            XML_FILE.delete();
        }
        Assert.assertFalse(XML_FILE.exists());

        // test getInputParameter if file doesn't exist
        service.getInputParameter(XML_FILE_NAME);
    }

    @Test
    public void testCreateMainJob() throws Exception {
        final Config config = Config.loadConfig(null, service.workItemService);
        config.setInputXmlLocation(INPUT_XML_LOCATION);

        // adjust config to use test xml
        Marshaller marshaller = JAXBContext.newInstance(Config.class).createMarshaller();
        String configFileName = INPUT_XML_LOCATION + "jobcreator-config.xml";
        marshaller.marshal(config, new File(configFileName));
        String oldConfigFileName = Config.getConfigFile();
        Config.setConfigFile(configFileName);

        service.createMainJobs(null);
        String expectedMailContent = service.buildSuccessMailContent(JobCreatorTestHelper
                .createInputParameterForMainJob());
        Mockito.verify(service.mailService).sendMail(config.getAdminMailRecipients().split(" "),
                "Job creation finished", expectedMailContent);
        Mockito.verify(service.launcher).launchJobCreator(config, XML_FILE_NAME);
        Assert.assertTrue(XML_FILE.exists());

        // reset config for next tests
        Config.setConfigFile(oldConfigFileName);
    }

    @Test
    public void testCreateSystemTests() throws Exception {
        final Config config = Config.loadConfig(null, service.workItemService);
        config.setInputXmlLocation(INPUT_XML_LOCATION);

        // adjust config to use test xml
        Marshaller marshaller = JAXBContext.newInstance(Config.class).createMarshaller();
        String configFileName = INPUT_XML_LOCATION + "jobcreator-config.xml";
        marshaller.marshal(config, new File(configFileName));
        String oldConfigFileName = Config.getConfigFile();
        Config.setConfigFile(configFileName);

        service.createInputXml(XML_FILE_NAME, JobCreatorTestHelper.createInputParameterForMainJob(), true);
        service.createSystemTestJobs(null);
        String expectedMailContent = service.buildSuccessMailContent(JobCreatorTestHelper
                .createInputParameterForSystemTests());
        Mockito.verify(service.mailService).sendMail(config.getAdminMailRecipients().split(" "),
                "Job creation finished", expectedMailContent);
        Mockito.verify(service.launcher).launchJobCreator(config, XML_FILE_NAME_ST);

        // reset config for next tests
        Config.setConfigFile(oldConfigFileName);
    }
}