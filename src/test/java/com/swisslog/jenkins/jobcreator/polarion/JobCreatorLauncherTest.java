/*
 * -----------------------------------------------------------------------------
 * Application     : WM 6
 * Revision        : $Revision: 376093 $
 * Revision date   : $Date: 2016-10-22 02:45:35 +0200 (Sa, 22 Okt 2016) $
 * Last changed by : $Author: b2yeowo $
 * URL             : $URL: http://almscdc.swisslog.com/repo/SWPD/Development/WM6/trunk/Software/WM6/wm6-tools/jenkins-jobcreator/src/test/java/com/swisslog/jenkins/jobcreator/polarion/JobCreatorLauncherTest.java $
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

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;

import com.polarion.alm.ws.client.tracker.TrackerWebService;
import com.polarion.alm.ws.client.types.tracker.WorkItem;
import com.swisslog.jenkins.jobcreator.bindings.InputParameter;
import com.swisslog.jenkins.jobcreator.polarion.config.Config;
import com.swisslog.jenkins.jobcreator.polarion.launcher.JobCreatorLauncher;
import com.swisslog.jenkins.jobcreator.polarion.launcher.JobCreatorLauncherImpl;
import com.swisslog.jenkins.jobcreator.polarion.workitem.WorkItemException;
import com.swisslog.jenkins.jobcreator.polarion.workitem.WorkItemServiceImpl;

public class JobCreatorLauncherTest {

    private static final Logger LOGGER = Logger.getLogger(JobCreatorLauncherTest.class);

    /*
     * This test actually creates a jenkins job. Therefore it doesn't use junit, so that it isn't executed
     * automatically.
     */
    public static void main(String[] args) throws JAXBException, IOException, WorkItemException {
        TrackerWebService trackerServiceMock = JobCreatorTestHelper.createTrackerWebServiceMock();

        WorkItem workItem = JobCreatorTestHelper.createWorkItemMock();
        Config config = Config.loadConfig(workItem, new WorkItemServiceImpl(trackerServiceMock));

        JobCreatorService service = new JobCreatorService(trackerServiceMock);
        InputParameter inputParameter = service.createInputParameterForMainJobs(workItem, config);
        // use the test view and not the country (e.g. 01-Switzerland)
        inputParameter.setParentView("Test Jobs");

        String inputFileName = service.createFileName(workItem, config, "");
        service.createInputXml(inputFileName, inputParameter, true);

        // start jenkins-jobcreator
        JobCreatorLauncher launcher = new JobCreatorLauncherImpl();
        launcher.launchJobCreator(config, inputFileName);

        LOGGER.info("Job creation finish. Please check if the job was created correctly.");
    }
}