/*
 * -----------------------------------------------------------------------------
 * Application     : WM 6
 * Revision        : $Revision: 376093 $
 * Revision date   : $Date: 2016-10-22 02:45:35 +0200 (Sa, 22 Okt 2016) $
 * Last changed by : $Author: b2yeowo $
 * URL             : $URL: http://almscdc.swisslog.com/repo/SWPD/Development/WM6/trunk/Software/WM6/wm6-tools/jenkins-jobcreator/src/main/java/com/swisslog/jenkins/jobcreator/polarion/JobCreatorService.java $
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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;

import com.polarion.alm.ws.client.tracker.TrackerWebService;
import com.polarion.alm.ws.client.types.tracker.WorkItem;
import com.swisslog.jenkins.jobcreator.bindings.InputParameter;
import com.swisslog.jenkins.jobcreator.polarion.config.Config;
import com.swisslog.jenkins.jobcreator.polarion.config.ConfigParameter;
import com.swisslog.jenkins.jobcreator.polarion.launcher.JobCreatorLauncher;
import com.swisslog.jenkins.jobcreator.polarion.launcher.JobCreatorLauncherImpl;
import com.swisslog.jenkins.jobcreator.polarion.mail.MailService;
import com.swisslog.jenkins.jobcreator.polarion.mail.MailServiceImpl;
import com.swisslog.jenkins.jobcreator.polarion.workitem.WorkItemException;
import com.swisslog.jenkins.jobcreator.polarion.workitem.WorkItemService;
import com.swisslog.jenkins.jobcreator.polarion.workitem.WorkItemServiceImpl;

/**
 * This service creates or adjustes an input.xml file and starts the jenkins-jobcreator. The user is informed by mail if
 * the creation was sucessful or not.
 */
public class JobCreatorService {

    private static final Logger LOGGER = Logger.getLogger(JobCreatorService.class);

    MailService mailService;
    JobCreatorLauncher launcher;
    WorkItemService workItemService;

    /**
     * Create a new JobCreatorService.
     *
     * @param trackerService
     *            the tracker web service used to call the work item service
     */
    public JobCreatorService(TrackerWebService trackerService) {
        super();
        this.mailService = new MailServiceImpl();
        this.launcher = new JobCreatorLauncherImpl();
        this.workItemService = new WorkItemServiceImpl(trackerService);
    }

    /**
     * Create input xml for main jobs and start jenkins-jobcreator
     *
     * @param workItem
     *            the central tools work item
     */
    public void createMainJobs(WorkItem workItem) {
        Config config = null;
        InputParameter inputParameter = null;
        try {
            config = Config.loadConfig(workItem, workItemService);

            // create input parameter
            inputParameter = createInputParameterForMainJobs(workItem, config);

            // create xml
            String inputFileName = createFileName(workItem, config, "");
            File inputXml = createInputXml(inputFileName, inputParameter, true);

            if (inputXml.exists()) {
                launcher.launchJobCreator(config, inputFileName);

                sendSuccessMail(inputParameter);
            } else {
                // creation of input xml file failed
                throw new FileNotFoundException("Could not find file " + inputXml.getName());
            }
        } catch (Exception e) {
            LOGGER.error("An exception occured while creating main jobs", e);
            sendFailureMail(config, inputParameter, e);
        }
    }

    /**
     * Create input xml for system test jobs and start jenkins-jobcreator
     *
     * @param workItem
     *            the central tools work item
     */
    public void createSystemTestJobs(WorkItem workItem) {
        Config config = null;
        InputParameter inputParameter = null;
        try {
            config = Config.loadConfig(workItem, workItemService);

            // create input parameter
            inputParameter = createInputParameterForSystemTestJobs(workItem, config);

            // create xml
            String inputFileName = createFileName(workItem, config, "-st");
            File inputXml = createInputXml(inputFileName, inputParameter, true);

            if (inputXml.exists()) {
                launcher.launchJobCreator(config, inputFileName);

                sendSuccessMail(inputParameter);
            } else {
                // creation of input xml file failed
                throw new FileNotFoundException("Could not find file " + inputXml.getName());
            }
        } catch (Exception e) {
            LOGGER.error("An exception occured while creating main jobs", e);
            sendFailureMail(config, inputParameter, e);
        }
    }

    /**
     * Create input parameter for main jobs
     *
     * @param workItem
     *            the central tools work item which contains all required data
     * @param config
     *            the config loaded from jobcreator-config.xml
     * @return the created input parameter
     * @throws WorkItemException
     * @throws RemoteException
     */
    public InputParameter createInputParameterForMainJobs(WorkItem workItem, Config config) throws WorkItemException,
            RemoteException {
        String parentView = config.getParentView();
        String projectName = config.get(ConfigParameter.FULL_NAME);

        InputParameter inputParameter = new InputParameter();
        inputParameter.setParentView(parentView);
        inputParameter.setProjectName(projectName);

        inputParameter.setAbortOnError(false);
        inputParameter.setCreateMainJob(true);
        inputParameter.setCreateSonarJob(true);
        inputParameter.setCreateWhitesourceJob(true);
        inputParameter.setCreateSystemTestJobs(false);

        // add parameter map
        Map<String, String> params = new HashMap<String, String>();
        params.put(InputParameter.PROJECT_NAME, projectName);
        params.put(InputParameter.LIST_NAME, projectName);
        params.put(InputParameter.SLAVE, config.getSlave());
        params.put(InputParameter.SVN_URL, workItemService.getSvnUrl(workItem));
        params.put(InputParameter.MAIL_RECIPIENTS, workItemService.getAuthorMail(workItem));
        params.put(InputParameter.ADMIN_MAIL_RECIPIENTS, config.getAdminMailRecipients());
        params.put(InputParameter.LOCK, config.getLock());
        params.put(InputParameter.JDK_NAME, config.getJdkName());
        params.put(InputParameter.MAIN_MVN_TARGETS, config.getMainMvnTargets());
        params.put(InputParameter.JACOCO_ST_FOLDER, config.get(ConfigParameter.FULL_NAME_LOWER_CASE));
        params.put(InputParameter.SONAR_BRANCH, config.get(ConfigParameter.SONAR_BRANCH));
        params.put(InputParameter.MODULE_NAME_LOWERCASE, config.get(ConfigParameter.PROJECT_NAME_LOWER_CASE));
        inputParameter.setParameterMap(params);

        LOGGER.info("Created input parameter for main jobs: " + inputParameter);
        return inputParameter;
    }

    /**
     * Load input parameter from a file
     *
     * @param fileName
     *            the file name
     * @return the input parameter
     * @throws JAXBException
     */
    public InputParameter getInputParameter(String fileName) throws JAXBException {
        Unmarshaller unmarshaller = JAXBContext.newInstance(InputParameter.class).createUnmarshaller();
        InputParameter inputParameter = unmarshaller.unmarshal(new StreamSource(fileName), InputParameter.class)
                .getValue();
        LOGGER.info("Loaded input parameter from file " + fileName + ": " + inputParameter);
        return inputParameter;
    }

    /**
     * Adjust an existing input parameter for system tests
     *
     * @param workItem
     *            the system tests work item which contains all required data
     * @param config
     *            the config loaded from jobcreator-config.xml
     * @param inputParameter
     *            the existing input parameter (can be loaded from a file by getInputParameter(fileName))
     * @return the adjusted input parameter
     * @throws WorkItemException
     * @throws RemoteException
     */
    public InputParameter createInputParameterForSystemTestJobs(WorkItem workItem, Config config)
            throws WorkItemException, RemoteException {
        String parentView = config.getParentView();
        String projectName = config.get(ConfigParameter.FULL_NAME);

        InputParameter inputParameter = new InputParameter();
        inputParameter.setParentView(parentView);
        inputParameter.setProjectName(projectName);

        inputParameter.setAbortOnError(false);
        inputParameter.setCreateMainJob(false);
        inputParameter.setCreateSonarJob(false);
        inputParameter.setCreateWhitesourceJob(false);
        inputParameter.setCreateSystemTestJobs(true);

        // add parameter map
        Map<String, String> params = inputParameter.getParameterMap();
        params.put(InputParameter.PROJECT_NAME, projectName);
        params.put(InputParameter.LIST_NAME, projectName);
        params.put(InputParameter.SLAVE, config.getSlave());
        params.put(InputParameter.SVN_URL, workItemService.getSvnUrl(workItem));
        params.put(InputParameter.MAIL_RECIPIENTS, workItemService.getAuthorMail(workItem));
        params.put(InputParameter.ADMIN_MAIL_RECIPIENTS, config.getAdminMailRecipients());
        params.put(InputParameter.LOCK, config.getLock());
        params.put(InputParameter.LOCK_ST, config.getSystemTestLock());
        params.put(InputParameter.JDK_NAME, config.getJdkName());
        params.put(InputParameter.MAIN_MVN_TARGETS, config.getMainMvnTargets());
        params.put(InputParameter.MODULE_NAME_LOWERCASE, config.get(ConfigParameter.PROJECT_NAME_LOWER_CASE));
        params.put(InputParameter.SONAR_BRANCH, config.get(ConfigParameter.SONAR_BRANCH));
        params.put(InputParameter.JACOCO_ST_FOLDER, config.get(ConfigParameter.FULL_NAME_LOWER_CASE));
        params.put(InputParameter.DATALOADER_CONFIGURATION, config.getDataLoaderConfiguration());
        params.put(InputParameter.DOMAIN_NAME, config.get(ConfigParameter.FULL_NAME_LOWER_CASE));
        params.put(InputParameter.SCREENSHOTS_DIR, config.get(ConfigParameter.FULL_NAME_LOWER_CASE));
        inputParameter.setParameterMap(params);

        LOGGER.info("Adjusted input parameter for system tests: " + inputParameter);
        return inputParameter;
    }

    /**
     * Create an xml file for the given input parameter
     *
     * @param fileName
     *            the file name (usually input-[project].xml)
     * @param inputParameter
     *            the input parameter which should be written to the file
     * @param overwrite
     *            if true the method overwrites the existing file. otherwise it throws an exception if the file already
     *            exists
     * @return the created or overwritten file
     * @throws IOException
     * @throws JAXBException
     */
    File createInputXml(String fileName, InputParameter inputParameter, boolean overwrite) throws IOException,
            JAXBException {
        // check if xml file already exists
        File xmlFile = new File(fileName);
        if (xmlFile.exists()) {
            if (overwrite) {
                LOGGER.warn("File " + fileName + " already exists -> overwriting ...");
            } else {
                throw new IOException("Could not create file " + fileName + " because it already exists");
            }
        }

        // create xml file
        Marshaller marshaller = JAXBContext.newInstance(InputParameter.class).createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(inputParameter, xmlFile);
        LOGGER.info("Wrote input parameter to file " + fileName);
        return xmlFile;
    }

    /**
     * Create the file name for a give workitem
     *
     * @param workItem
     *            the work item which contains the project name
     * @param config
     *            the config which specifies the target folder
     * @return the file name
     * @throws WorkItemException
     * @throws RemoteException
     */
    String createFileName(WorkItem workItem, Config config, String suffix) throws WorkItemException, RemoteException {
        String projectName = config.get(ConfigParameter.FULL_NAME).toLowerCase().replaceAll(" ", "");
        String inputXmlLocation = config.getInputXmlLocation();
        if (!inputXmlLocation.equals("") && !inputXmlLocation.endsWith("\\")) {
            inputXmlLocation += "\\";
        }

        return inputXmlLocation + "input-" + projectName + suffix + ".xml";
    }

    /**
     * Send a mail after successful jobcreation for the given input parameter
     *
     * @param inputParameter
     *            the input parameter which was used for job creation
     */
    public void sendSuccessMail(InputParameter inputParameter) {
        String adminMailRecipients = inputParameter.getParameterMap().get(InputParameter.ADMIN_MAIL_RECIPIENTS);
        String[] receivers = adminMailRecipients == null ? new String[0] : adminMailRecipients.split(" ");

        try {
            mailService.sendMail(receivers, "Job creation finished", buildSuccessMailContent(inputParameter));
        } catch (Exception e) {
            LOGGER.error("Could not send notification. Please, check mail settings for Polarion.", e);
        }
    }

    /**
     * Build the content for a mail after successful jobcreation for the given input parameter
     *
     * @param inputParameter
     *            the input parameter which was used for job creation
     * @return the mail content
     */
    public String buildSuccessMailContent(InputParameter inputParameter) {
        StringBuilder contentBuilder = new StringBuilder();
        contentBuilder.append("<html><body>");
        contentBuilder.append("<b>Job creation finished for project ").append(inputParameter.getProjectName())
                .append("</b>");

        contentBuilder.append("<p>Created jobs:</p><ul>");
        if (inputParameter.getCreateMainJob()) {
            contentBuilder.append("<li>Main Job</li>");
        }
        if (inputParameter.getCreateSonarJob()) {
            contentBuilder.append("<li>Sonar Job</li>");
        }
        if (inputParameter.getCreateWhitesourceJob()) {
            contentBuilder.append("<li>Whitesource Job</li>");
        }
        if (inputParameter.getCreateSystemTestJobs()) {
            contentBuilder.append("<li>System Test Jobs</li>");
        }
        contentBuilder.append("</ul></body></html>");
        return contentBuilder.toString();
    }

    /**
     * Send a mail after jobcreation failed
     *
     * @param config
     *            the config which was used for job creation
     * @param inputParameter
     *            the input parameter which was used for job creation
     * @param exception
     *            the exception which occurred during job creation
     */
    public void sendFailureMail(Config config, InputParameter inputParameter, Exception exception) {
        if (config == null) {
            try {
                config = Config.loadWithoutWorkItem();
            } catch (JAXBException e) {
                LOGGER.error("Could not load configuration", e);
            }
        }

        if (config == null) {
            LOGGER.error("Job creation failed and could not send notification");
            return;
        }

        String adminMailRecipients = config.getAdminMailRecipients();
        String[] receivers = adminMailRecipients == null ? new String[0] : adminMailRecipients.split(" ");

        try {
            mailService.sendMail(receivers, "Job creation failed",
                    buildFailureMailContent(config, inputParameter, exception));
        } catch (Exception e) {
            LOGGER.error("Could not send notification. Please, check mail settings for Polarion.", e);
        }
    }

    /**
     * Build the mail content for a mail after jobcreation failed
     *
     * @param config
     *            the config which was used for job creation
     * @param inputParameter
     *            the input parameter which was used for job creation
     * @param exception
     *            the exception which occurred during job creation
     * @return the mail content
     */
    public String buildFailureMailContent(Config config, InputParameter inputParameter, Exception exception) {
        if (inputParameter == null) {
            StringBuilder contentBuilder = new StringBuilder();
            contentBuilder.append("<html><body>");
            contentBuilder.append("<b>Job creation failed</b>");
            if (exception != null) {
                contentBuilder.append("<p>" + exception.getClass().getName() + ": " + exception.getMessage() + "</p>");
            }
            contentBuilder.append("<p>Please check log files for more information</p>");
            return contentBuilder.toString();
        }

        StringBuilder contentBuilder = new StringBuilder();
        contentBuilder.append("<html><body>");
        contentBuilder.append("<b>Job creation failed for project ").append(inputParameter.getProjectName())
                .append("</b>");
        if (exception != null) {
            contentBuilder.append("<p>" + exception.getClass().getName() + ": " + exception.getMessage() + "</p>");
        }
        contentBuilder.append("<p>Please check log files for more information</p>");

        contentBuilder.append("<p>Tried to create jobs:</p><ul>");
        if (inputParameter.getCreateMainJob()) {
            contentBuilder.append("<li>Main Job</li>");
        }
        if (inputParameter.getCreateSonarJob()) {
            contentBuilder.append("<li>Sonar Job</li>");
        }
        if (inputParameter.getCreateWhitesourceJob()) {
            contentBuilder.append("<li>Whitesource Job</li>");
        }
        if (inputParameter.getCreateSystemTestJobs()) {
            contentBuilder.append("<li>System Test Jobs</li>");
        }
        contentBuilder.append("</ul></body></html>");
        return contentBuilder.toString();
    }
}