package com.swisslog.jenkins.jobcreator.polarion;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.JAXBException;
import javax.xml.rpc.ServiceException;

import org.apache.log4j.Logger;

import com.polarion.alm.ws.client.WebServiceFactory;
import com.polarion.alm.ws.client.session.SessionWebService;
import com.polarion.alm.ws.client.tracker.TrackerWebService;
import com.polarion.alm.ws.client.types.tracker.EnumOptionId;
import com.polarion.alm.ws.client.types.tracker.WorkItem;
import com.swisslog.jenkins.jobcreator.polarion.config.Config;

/**
 * The post-commit.bat is executed after a new revision is committed. The script starts this class to check if a jenkins
 * job needs to be created. This class is called outside of polarion. Therefore we need to use the web services (e.g.
 * TrackerWebService instead of TrackerService)
 */
public class PostCommitHook {

    private static final Logger LOGGER = Logger.getLogger(JobCreatorService.class);

    static final String STATUS = "review";
    static final String TRACKER_PROJECT_ID = "ProductTracker";
    static final String CENTRAL_TOOLS = "centralTools";
    static final String SYSTEM_TESTS = "systemTests";

    static final String PROPERTY_POLARION_USER = "polarion.user";
    static final String PROPERTY_POLARION_PASSWORD = "polarion.password";
    static final String PROPERTY_POLARION_WEBSERVICE_URL = "polarion.webservice.url";

    static final int TIMEOUT = 60 * 1000;
    static final String SVNLOOK_DIR = "D:\\Polarion\\data\\svn\\repo";
    static final String WORKITEM_XML = "/workitem.xml";
    static final String WORKITEM_ID_PREFIX = "TRK-";

    static String polarionUser;
    static String polarionPassword;
    static String polarionWebserviceUrl;

    private final String repoPath;
    private final String revision;

    WebServiceFactory factory;
    SessionWebService sessionService;
    TrackerWebService trackerService;
    JobCreatorService jobCreatorService;

    /**
     * Start the post commit hook
     *
     * @param args
     *            this method requires three arguments: repoPath, revision and configFile
     */
    public static void main(final String[] args) {
        LOGGER.info("-------- Starting post-commit hook --------");

        if (args.length != 4) {
            LOGGER.fatal("Wrong number of arguments!");
            return;
        }

        Config.setConfigFile(args[2]);

        loadEnvironment(args[3]);

        try {
            WebServiceFactory webServiceFactory = new WebServiceFactory(polarionWebserviceUrl);
            PostCommitHook hook = new PostCommitHook(args[0], args[1], webServiceFactory);
            if (!hook.runWithTimeout()) {
                LOGGER.error("PostCommitHook finished with errors");
            }
        } catch (MalformedURLException e) {
            LOGGER.error("An exception occurred during post commit hook", e);
        }

        LOGGER.info("-------- Post-commit hook finished --------");
    }

    static void loadEnvironment(String propertiesFile) {
        Properties env = new Properties();
        InputStream input = null;
        try {
            input = new FileInputStream(propertiesFile);
            env.load(input);

            polarionUser = env.getProperty(PROPERTY_POLARION_USER);
            polarionPassword = env.getProperty(PROPERTY_POLARION_PASSWORD);
            polarionWebserviceUrl = env.getProperty(PROPERTY_POLARION_WEBSERVICE_URL);

            // url must always end with / otherwise polarion cannot connect
            if (!polarionWebserviceUrl.endsWith("/")) {
                polarionWebserviceUrl += "/";
            }

            LOGGER.info("Loaded environment (polarion.user=" + polarionUser + ", polarion.webservice.url="
                    + polarionWebserviceUrl + ")");
        } catch (IOException e) {
            LOGGER.error("Could not load environment from " + propertiesFile, e);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }

    /**
     * Create a new PostCommitHook and connect to polarion.
     *
     * @param repoPath
     *            the repository path
     * @param revision
     *            the revision
     * @param factory
     *            the web service factory
     */
    public PostCommitHook(String repoPath, String revision, WebServiceFactory factory) {
        super();
        this.repoPath = repoPath;
        this.revision = revision;
        this.factory = factory;
    }

    /**
     * Connect to polarion and retrieve the web services
     *
     * @throws ServiceException
     *             throws a ServiceException if connection to polarion failed
     * @throws RemoteException
     *             throws a RemoteException if connection to polarion failed
     */
    void setUpSession() throws ServiceException, RemoteException {
        System.out.println("Setting up Session");
        sessionService = factory.getSessionService();
        trackerService = factory.getTrackerService();
        jobCreatorService = new JobCreatorService(trackerService);

        sessionService.logIn(polarionUser, polarionPassword);
    }

    /**
     * Run the post commit hook with a timeout to avoid a never-ending process running in the background.
     *
     * @return false if an exception occurred
     */
    public boolean runWithTimeout() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        final Future<Boolean> handler = executor.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return run();
            }
        });

        try {
            return handler.get(TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            handler.cancel(true);
            LOGGER.error("An exeception occurred during post-commit hook", e);
            return false;
        } finally {
            executor.shutdownNow();
        }
    }

    /**
     * Run the post commit hook (check if a jenkins job needs to be created)
     *
     * @return false if an exception occurred
     */
    public boolean run() {
        try {
            setUpSession();
            String workItemId = getChangedWorkItemId();
            if (workItemId == null) {
                LOGGER.info("Commit contains no work item. -> Skip job creation");
                return true;
            }
            WorkItem workItem = getWorkItem(workItemId);
            createJobs(workItem);
            return true;
        } catch (Exception e) {
            LOGGER.error("An exception occurred during post-commit hook", e);
            return false;
        } finally {
            if (sessionService != null) {
                try {
                    sessionService.endSession();
                } catch (RemoteException e) {
                }
            }
        }
    }

    /**
     * Get the changed workitem.xml and extract the id from its path. If there is no workitem.xml in the revision it
     * returns null.
     *
     * @return the work item id or null
     * @throws IOException
     *             throws an IOException if svnlook command failed
     * @throws JAXBException
     */
    String getChangedWorkItemId() throws IOException, JAXBException {
        Config config = Config.loadWithoutWorkItem();

        StringBuffer buf = new StringBuffer();
        buf.append(config.getSvnlookExe()).append(" changed ").append(SVNLOOK_DIR).append(" -r ").append(revision);

        Process proc = Runtime.getRuntime().exec(buf.toString());
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line;
            LOGGER.debug("Looking for work item in changed files ...");
            while ((line = reader.readLine()) != null) {
                String workItemId = getWorkItemIdFromLine(line);
                if (workItemId != null && !workItemId.equals("")) {

                    if (workItemId.startsWith(WORKITEM_ID_PREFIX)) {
                        // we need only tracker work items
                        return workItemId;
                    } else {
                        LOGGER.info("Found work item which doesn't start with prefix " + WORKITEM_ID_PREFIX
                                + " -> ignore it");
                    }
                }
            }
            LOGGER.info("No workitems found");
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        LOGGER.warn("Could not get work item id");
        return null;
    }

    /**
     * Get the work item from a line of the "svnlook changed" command
     *
     * @param line
     *            the line from "svnlook changed" command
     * @return the work item id or null
     */
    String getWorkItemIdFromLine(String line) {
        if (line.endsWith(WORKITEM_XML)) {
            String workItemId = line.substring(0, line.length() - WORKITEM_XML.length());
            workItemId = workItemId.substring(workItemId.lastIndexOf("/") + 1);
            LOGGER.info("Work item id is " + workItemId);
            return workItemId;
        } else {
            return null;
        }
    }

    /**
     * Get the work item from polarion
     *
     * @param workItemId
     *            the work item id
     * @return the work item
     * @throws RemoteException
     *             throws a RemoteException if connection to polarion failed
     * @throws IllegalArgumentException
     *             throws an IllegalArgumentException if the work item id is invalid
     * @throws IllegalStateException
     *             throws an IllegalStateException if the work item is unresolvable
     */
    WorkItem getWorkItem(String workItemId) throws RemoteException {
        StringBuffer query = new StringBuffer("(project.id:").append(TRACKER_PROJECT_ID).append(" AND id:")
                .append(workItemId).append(")");
        String[] fields = { "id" };

        WorkItem[] workItems = trackerService.queryWorkItems(query.toString(), null, fields);

        if (workItems == null || workItems.length == 0) {
            throw new IllegalArgumentException("Could not find work item with id " + workItemId);
        } else {
            if (workItems[0].isUnresolvable()) {
                throw new IllegalStateException("Work item " + workItemId + " is unresolvable");
            } else {
                LOGGER.info("Found work item for id " + workItemId + ": " + workItems[0]);

                // if the status is null, try to reload the work item for the current revision
                if (workItems[0].getStatus() == null) {
                    LOGGER.error("WorkItem status is null -> getting work item in current revision ...");
                    String uri = workItems[0].getUri();
                    WorkItem workItem = trackerService.getWorkItemByUriInRevision(uri, revision);

                    if (workItem == null || workItem.getStatus() == null) {
                        LOGGER.error("Status is still null");
                        throw new IllegalStateException("Status of work item is null");
                    }
                    return workItem;
                }

                return workItems[0];
            }
        }
    }

    /**
     * Execute job creator service if a job needs to be created for the given work item
     *
     * @param workItem
     *            the work item
     */
    void createJobs(WorkItem workItem) {
        EnumOptionId statusOption = workItem.getStatus();
        if (statusOption == null) {
            LOGGER.error("Work item status is null");
            return;
        }

        String status = statusOption.getId();
        if (STATUS.equals(status)) {
            LOGGER.info("Work item is in status " + status + " -> starting job creator ...");

            String type = workItem.getType().getId();
            if (CENTRAL_TOOLS.equals(type)) {
                LOGGER.info("Creating main jobs ...");
                jobCreatorService.createMainJobs(workItem);
            } else if (SYSTEM_TESTS.equals(type)) {
                LOGGER.info("Creating system tests ...");
                jobCreatorService.createSystemTestJobs(workItem);
            } else {
                LOGGER.info("Work type: " + type + " -> No job needs to be created");
            }
        } else {
            LOGGER.info("Work item is in status " + status + " -> job creation not neccessary");
        }
    }
}