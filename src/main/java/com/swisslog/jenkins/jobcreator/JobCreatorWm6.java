package com.swisslog.jenkins.jobcreator;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JOptionPane;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import com.swisslog.jenkins.jobcreator.bindings.Config;
import com.swisslog.jenkins.jobcreator.bindings.InputParameterModule;
import com.swisslog.jenkins.jobcreator.bindings.InputParameterWm6;

/**
 * JobCreator creates a project view and multiple WM6 jenkins jobs..
 */
public class JobCreatorWm6 {

    /** Logger */
    private static final Logger LOGGER = Logger.getLogger(JobCreatorWm6.class.getName());

    /** Standard web resource type */
    private static final String WEB_RESOURCE_TYPE = "application/xml";

    /** Jenkins base Path */
    private static final String JENKINS_BASE_PATH = "/jenkins";

    /** View Path */
    private static final String VIEW_PATH = "/view/";

    /** Name query argument */
    private static final String NAME_ARGUMENT = "name=";

    /** The input parameter for the display name */
    private static final String PARAM_DISPLAY_NAME = "%DISPLAY_NAME%";

    /** The input parameter for the project name */
    private static final String PARAM_LIST_NAME = "%LIST_NAME%";

    /** The input parameter for the project name without spaces */
    private static final String PARAM_LIST_NAME_WITHOUT_SPACES = "%LIST_NAME_WITHOUT_SPACES%";

    /** The config xml binding containing all project independent configuration */
    private Config config;

    /** The project specific input parameters. */
    private InputParameterWm6 inputParameter;

    /** HTTP client */
    private HttpClient httpClient;

    /** HTTP client context */
    private HttpClientContext httpClientContext;

    /** Failed job creation counter */
    private int counterFailedJobCreation = 0;

    /** Successful job creation counter */
    private int counterSuccessfulJobCreation = 0;

    /**
     * Instantiates a new job creator.
     * <p>
     * Initializes the logging, loads the config & input xml bindings and starts a http client
     *
     * @param configFile
     *            the config file
     * @param inputFile
     *            the input file
     * @throws JobCreatorException
     *             the job creator tool exception
     */
    public JobCreatorWm6(String configFile, String inputFile) throws JobCreatorException {
        loadConfiguration(configFile, inputFile);
        createHttpClient();
    }

    /**
     * Load configuration and input parameters.
     *
     * @param configFile
     *            the config file
     * @param inputFile
     *            the input file
     * @throws JobCreatorException
     *             the job creator tool exception
     */
    private void loadConfiguration(String configFile, String inputFile) throws JobCreatorException {
        try {
            LOGGER.info("Loading configuration. ConfigFile [" + configFile + "], inputMap [" + inputFile + "]");

            Unmarshaller configUnmarshaller = JAXBContext.newInstance(Config.class).createUnmarshaller();
            this.config = configUnmarshaller.unmarshal(new StreamSource(configFile), Config.class).getValue();

            Unmarshaller inputUnmarshaller = JAXBContext.newInstance(InputParameterWm6.class).createUnmarshaller();
            this.inputParameter = inputUnmarshaller.unmarshal(new StreamSource(inputFile), InputParameterWm6.class)
                    .getValue();

        } catch (JAXBException e) {
            throw new JobCreatorException("Unable to load configuration & input parameter.", e);
        }

    }

    /**
     * Creates the http client, including basic authentication prepared for the jenkins host
     */
    private void createHttpClient() {
        LOGGER.info("Creating http client");

        // Create http client
        HttpHost httpHost = new HttpHost(config.getHostName(), config.getHostPort(), "http");

        AuthScope authScope = new AuthScope(httpHost.getHostName(), httpHost.getPort());
        UsernamePasswordCredentials upCredentials = new UsernamePasswordCredentials(config.getHostUsername(),
                config.getHostPassword());

        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(authScope, upCredentials);

        this.httpClient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build();

        // Create AuthCache instance
        AuthCache authCache = new BasicAuthCache();
        BasicScheme basicAuth = new BasicScheme();
        authCache.put(httpHost, basicAuth);

        // Add AuthCache to the execution context
        this.httpClientContext = HttpClientContext.create();
        this.httpClientContext.setAuthCache(authCache);
    }

    /**
     * Generates the jenkins URI supporting escaping of paths & query parameers..
     *
     * @param path
     *            the path
     * @param query
     *            the query
     * @return the uri
     * @throws URISyntaxException
     *             the uRI syntax exception
     */
    private URI generateURI(String path, String query) throws URISyntaxException {
        return new URI(config.getHostScheme(), null, config.getHostName(), config.getHostPort(), path, query, null);
    }

    /**
     * Does verify a http client response to the http code 200. IF a different code is returned, a
     * JobCreatorToolException is thrown. This may be prevented by the input paraemter "AbortOnError=false".
     *
     * @param response
     *            the response
     * @return true, if successful, false if failed (and creator shall not be aborted)
     * @throws JobCreatorException
     *             the job creator tool exception
     */
    private boolean verifyResponse(HttpResponse response) throws JobCreatorException, IOException {

        // Consume the response content
        HttpEntity entityConsume = response.getEntity();
        EntityUtils.consume(entityConsume);

        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            this.counterFailedJobCreation++;
            if (inputParameter.getAbortOnError()) {
                throw new JobCreatorException(
                        "Client response [" + response.getStatusLine().getStatusCode() + "] operation aborted");
            } else {
                return false;
            }
        } else {
            this.counterSuccessfulJobCreation++;
            return true;
        }
    }

    /**
     * Generate a http post request.
     *
     * @param uri
     *            the uri
     * @param xml
     *            the xml to be sent
     * @return the httpPostRequest
     * @throws URISyntaxException
     *             the uRI syntax exception
     * @throws UnsupportedEncodingException
     *             the unsupported encoding exception
     */
    private HttpPost generatePostRequest(URI uri, String xml) throws URISyntaxException, UnsupportedEncodingException {
        HttpPost postRequest = new HttpPost(uri);

        StringEntity input = new StringEntity(xml);
        input.setContentType(WEB_RESOURCE_TYPE);
        postRequest.setEntity(input);

        return postRequest;
    }

    /**
     * Generates the project view on a given parent view configured with the input parameters.
     *
     * @throws JobCreatorException
     *             the job creator tool exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws URISyntaxException
     *             the uRI syntax exception
     */
    private String generateProjectView() throws JobCreatorException, IOException, URISyntaxException {

        return generateView(inputParameter.getProjectName(),
                JENKINS_BASE_PATH + VIEW_PATH + inputParameter.getParentView(), true);
    }

    /**
     * Generates the project view on a given parent view configured with the input parameters.
     *
     * @throws JobCreatorException
     *             the job creator tool exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws URISyntaxException
     *             the uRI syntax exception
     */
    private String generateView(String viewName, String parentView, boolean nestedView)
            throws JobCreatorException, IOException, URISyntaxException {

        inputParameter.getParameterMap().put("%VIEW_NAME%", viewName);
        TemplateWm6 template = new TemplateWm6(
                new File(config.getTemplatesDirectory(), nestedView ? "tmpl-view-nested.xml" : "tmpl-view.xml"),
                inputParameter);

        String path = parentView + "/createView";
        String query = NAME_ARGUMENT + viewName;
        URI uri = generateURI(path, query);

        LOGGER.info("Creating project view [" + viewName + "] URI [" + uri + "]");

        HttpPost postRequest = generatePostRequest(uri, template.getTemplateContent());
        HttpResponse response = httpClient.execute(postRequest, httpClientContext);

        if (verifyResponse(response)) {
            LOGGER.info("Project project view [" + viewName + "] successfully created. ");
        } else {
            LOGGER.warn("Unable to create project view [" + viewName + "]. Continue ...");
        }
        return parentView + VIEW_PATH + viewName;
    }

    /**
     * Adds the already exisitng job to the parentView.
     *
     * @throws JobCreatorException
     *             the job creator tool exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws URISyntaxException
     *             the uRI syntax exception
     */
    private void addJobToView(String jobName, String parentView)
            throws JobCreatorException, IOException, URISyntaxException {

        String path = parentView + "/addJobToView";
        String query = NAME_ARGUMENT + jobName;
        URI uri = generateURI(path, query);

        LOGGER.info("Add job to view [" + jobName + "] URI [" + uri + "]");

        HttpPost postRequest = new HttpPost(uri);
        HttpResponse response = httpClient.execute(postRequest, httpClientContext);

        if (verifyResponse(response)) {
            LOGGER.info("successfully added job to view [" + parentView + "]. ");
        } else {
            LOGGER.warn("Unable to add job to view [" + parentView + "]. Continue ...");
        }
    }

    /**
     * Creates a jenkins job
     *
     * @param jobName
     *            the job name
     * @param templateFile
     *            the template xml to be used
     * @param addToMainView
     *            shall the job be added to the project view
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws URISyntaxException
     *             the uRI syntax exception
     * @throws JobCreatorException
     *             the job creator tool exception
     * @return the job name with underscores instead of spaces
     */
    private String createJenkinsJob(String jobName, String templateFile, String parentView)
            throws IOException, URISyntaxException, JobCreatorException {

        // display original job name but use job name without spaces
        inputParameter.getParameterMap().put(PARAM_DISPLAY_NAME, jobName);
        jobName = jobName.replaceAll(" ", "_");
        String listNameWithoutSpaces = inputParameter.getParameterMap().get(PARAM_LIST_NAME).replaceAll(" ", "_");
        inputParameter.getParameterMap().put(PARAM_LIST_NAME_WITHOUT_SPACES, listNameWithoutSpaces);

        TemplateWm6 template = new TemplateWm6(new File(config.getTemplatesDirectory(), templateFile), inputParameter);

        URI uri;
        if (parentView.length() > 0) {
            // uri with parent view
            uri = generateURI(parentView + "/createItem", "name=" + jobName);
        } else {
            // uri jenkins direct
            uri = generateURI(JENKINS_BASE_PATH + "/createItem", NAME_ARGUMENT + jobName);
        }

        LOGGER.info("Creating job [" + jobName + "] with template file [" + templateFile + "] on URI [" + uri + "]");

        HttpPost postRequest = generatePostRequest(uri, template.getTemplateContent());
        HttpResponse response = httpClient.execute(postRequest, httpClientContext);

        if (verifyResponse(response)) {
            LOGGER.info("Job [" + jobName + "] successfully created. ");
        } else {
            LOGGER.warn("Unable to create job [" + jobName + "]. Continue ...");
        }

        return jobName;
    }

    /**
     * Does create a project view and a list of jenkins jobs, if enabled in the input parameters.
     * <p>
     * <ul>
     * <li>main job, template-main.xml, added to project view
     * <li>sonar job, template-sonar.xml, added to project view
     * <li>system test dataloader job, template-st-dataloader.xml
     * <li>system test start server job, template-st-startserver.xml
     * <li>system test deploy job, template-st-deploy.xml
     * <li>system test run tests job, template-st-tests.xml
     * <li>system test undeploy job, template-st-undeploy.xml
     * <li>system test stop server job, template-st-stopserver.xml
     * </ul>
     *
     * @throws JobCreatorException
     *             an exception containing root cause (IO/URI etc.)
     */
    public void createJobs() throws JobCreatorException {

        try {

            this.counterSuccessfulJobCreation = 0;
            this.counterFailedJobCreation = 0;

            // Main view
            String projectView = generateProjectView();

            String view01 = "";
            // create 01 build, integration test, sonar view
            if (inputParameter.getCreateMainJob() || inputParameter.getCreateIntegrationTestHsqlJob()
                    || inputParameter.getCreateIntegrationTestOracleJob() || inputParameter.getCreateSonarJob()) {
                view01 = generateView("01 - " + inputParameter.getProjectName() + " Build", projectView, false);
            }

            // Main build job
            if (inputParameter.getCreateMainJob()) {
                String jobName = inputParameter.getProjectName() + config.getMainJobSuffix();
                createJenkinsJob(jobName, "tmpl-main-build.xml", view01);
            }

            // integration-test hsql job
            if (inputParameter.getCreateIntegrationTestHsqlJob()) {
                String jobName = inputParameter.getProjectName() + config.getIntegrationTestHsqlSuffix();
                createJenkinsJob(jobName, "tmpl-integrationtest-hsql.xml", view01);
            }

            // integration-test oracle job
            if (inputParameter.getCreateIntegrationTestOracleJob()) {
                String jobName = inputParameter.getProjectName() + config.getIntegrationTestOracleSuffix();
                createJenkinsJob(jobName, "tmpl-integrationtest-oracle.xml", view01);
            }

            // integration-test sqlserver job
            if (inputParameter.getCreateIntegrationTestSqlJob()) {
                String jobName = inputParameter.getProjectName() + config.getIntegrationTestSqlSuffix();
                createJenkinsJob(jobName, "tmpl-integrationtest-sql.xml", view01);
            }

            // Sonar job
            if (inputParameter.getCreateSonarJob()) {
                String jobName = inputParameter.getProjectName() + config.getSonarJobSuffix();
                createJenkinsJob(jobName, "tmpl-sonar.xml", view01);
            }

            // System tests
            if (inputParameter.getCreateSystemTestJobs()) {

                String view98 = generateView("98 - " + inputParameter.getProjectName() + " - System Test Sequence",
                        projectView, false);
                String view99 = generateView("99 - " + inputParameter.getProjectName() + " - System Test Results",
                        projectView, false);

                /*
                 * Create System Test Build
                 */
                String jobNameStBuild = inputParameter.getProjectName() + " - 01 System Test Build";
                createJenkinsJob(jobNameStBuild, "tmpl-st-build.xml", view98);

                for (InputParameterModule module : inputParameter.getModules()) {

                    // update inputParameters with module parameters
                    inputParameter.getParameterMap().putAll(module.getModuleParameterMap());

                    String view = generateView(module.getModuleNumber() + " - " + inputParameter.getProjectName()
                            + " - System Tests (" + module.getModuleParameterMap().get("%MODULE_NAME%") + ")",
                            projectView, false);

                    /*
                     * Create System test jobs per module
                     */
                    int moduleJobNum = 1;
                    for (String template : module.getModuleTemplates()) {

                        String jobName = inputParameter.getProjectName() + " - "
                                + module.getModuleParameterMap().get("%MODULE_NAME%") + " - 0" + moduleJobNum + "-"
                                + module.getJobSuffix(template);
                        jobName = createJenkinsJob(jobName, template + ".xml", view);
                        if (module.isSystemTestJob(template)) {
                            addJobToView(jobName, view99);
                        }

                        // update inputMap
                        inputParameter.getParameterMap().put("%PHASE_NAME_" + moduleJobNum + "%",
                                module.getJobSuffix(template));
                        inputParameter.getParameterMap().put("%JOB_NAME_" + moduleJobNum + "%", jobName);

                        moduleJobNum++;
                    }

                    /*
                     * Create System Test Sequence
                     */
                    String jobName = inputParameter.getProjectName() + " - " + module.getModuleNumber() + " "
                            + module.getModuleParameterMap().get("%MODULE_NAME%") + " System Test Sequence";
                    createJenkinsJob(jobName, module.getModuleSequenceTemplate() + ".xml", view98);

                }

                // Cleanup job
                if (inputParameter.getCreateCleanupJob()) {
                    String jobName = inputParameter.getProjectName() + config.getCleanupJobSuffix();
                    createJenkinsJob(jobName, "tmpl-cleanup.xml", "");
                }

                /*
                 * Create System Test Sequence - all
                 */
                String jobNameSequenceAll = inputParameter.getProjectName() + " - 00 System Test Sequence (all)";
                createJenkinsJob(jobNameSequenceAll, "tmpl-st-sequence-all.xml", view98);

            }

            LOGGER.info("View/jobs created successfully: " + this.counterSuccessfulJobCreation + ", failed: "
                    + this.counterFailedJobCreation);

        } catch (IOException e) {
            throw new JobCreatorException("View/job creation failed with IO exception", e);
        } catch (URISyntaxException e) {
            throw new JobCreatorException("View/job creation failed with URI exception", e);
        }

    }

    /**
     * The main method creates a JobCreator instance and starts the creation of jenkins tasks.
     *
     * @param args
     *            the cli paramters args[0] = config.xml, args[1] = input.xml
     */
    public static void main(String[] args) {
        try {

            if (args.length == 0) {
                JobCreatorWm6 jenkinsTool = new JobCreatorWm6("install/config-wm6.xml", "install/input/"
                        + JOptionPane.showInputDialog("Please enter input-filename! e.g.: input-wm6-15.xml"));
                jenkinsTool.createJobs();
            } else if (args.length == 2) {
                JobCreatorWm6 jenkinsTool = new JobCreatorWm6(args[0], args[1]);
                jenkinsTool.createJobs();
            } else {
                LOGGER.error("Missing parameter. Usage: JobCreator config.xml input.xml");
            }

        } catch (Exception e) {
            LOGGER.error("Exception occured. Message=" + e.getMessage(), e);
        }

        LOGGER.info("Execution finished.");

    }

}