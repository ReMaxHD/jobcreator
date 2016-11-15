package com.swisslog.jenkins.jobcreator;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.apache.http.Header;
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
import com.swisslog.jenkins.jobcreator.bindings.InputParameter;
import com.swisslog.jenkins.jobcreator.bindings.JdkInstallation;

/**
 * JobCreator creates a project view and multiple WM6 jenkins jobs..
 */
public class JobCreator {

    /** Logger */
    private static final Logger LOGGER = Logger.getLogger(JobCreator.class.getName());

    /** Standard web resource type */
    private static final String WEB_RESOURCE_TYPE = "application/xml";

    /** Jenkins base Path */
    private static final String JENKINS_BASE_PATH = "/jenkins";

    /** View Path */
    private static final String VIEW_PATH = "/view/";

    /** Name query argument */
    private static final String NAME_ARGUMENT = "name=";

    /** The input parameter for the jdk version */
    private static final String PARAM_JDK = "%JDK%";

    /** The input parameter for the slave */
    private static final String PARAM_SLAVE = "%SLAVE%";

    /** The input parameter for the jdk installation name */
    private static final String PARAM_JDK_NAME = "%JDK_NAME%";

    /** The input parameter for the display name */
    private static final String PARAM_DISPLAY_NAME = "%DISPLAY_NAME%";

    /** The input parameter for the project name without spaces */
    private static final String PARAM_PROJECT_NAME_WITHOUT_SPACES = "%PROJECT_NAME_WITHOUT_SPACES%";

    /** The header of the HttpResponse which contains the error code */
    private static final String RESPONSE_ERROR_HEADER = "X-Error";

    /** The config xml binding containing all project independent configuration */
    private Config config;

    /** The project specific input parameters. */
    private InputParameter inputParameter;

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
    public JobCreator(String configFile, String inputFile) throws JobCreatorException {
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

            Unmarshaller inputUnmarshaller = JAXBContext.newInstance(InputParameter.class).createUnmarshaller();
            this.inputParameter = inputUnmarshaller.unmarshal(new StreamSource(inputFile), InputParameter.class)
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
    private void generateProjectView() throws JobCreatorException, IOException, URISyntaxException {

        Template template = new Template(new File(config.getTemplatesDirectory(), "template-view.xml"), inputParameter);

        String path = JENKINS_BASE_PATH + VIEW_PATH + inputParameter.getParentView() + "/createView";
        String query = NAME_ARGUMENT + inputParameter.getProjectName();
        URI uri = generateURI(path, query);

        LOGGER.info("Creating project view [" + inputParameter.getProjectName() + "] URI [" + uri + "]");

        HttpPost postRequest = generatePostRequest(uri, template.getTemplateContent());
        HttpResponse response = httpClient.execute(postRequest, httpClientContext);

        if (verifyResponse(response)) {
            LOGGER.info("Project project view [" + inputParameter.getProjectName() + "] successfully created.");
        } else {
            Header[] headers = response.getHeaders(RESPONSE_ERROR_HEADER);
            if (headers.length > 0) {
                LOGGER.warn("Unable to create project view [" + inputParameter.getProjectName() + "]: " + headers[0]);
            } else {
                LOGGER.warn("Unable to create project view [" + inputParameter.getProjectName() + "]: "
                        + response.getStatusLine());
            }
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
     */
    private void createJenkinsJob(String jobName, String templateFile, boolean addToMainView)
            throws IOException, URISyntaxException, JobCreatorException {

        // display original job name but use job name without spaces
        inputParameter.getParameterMap().put(PARAM_DISPLAY_NAME, jobName);
        jobName = jobName.replaceAll(" ", "_");
        String projectNameWithoutSpaces = inputParameter.getProjectName().replaceAll(" ", "_");
        inputParameter.getParameterMap().put(PARAM_PROJECT_NAME_WITHOUT_SPACES, projectNameWithoutSpaces);

        Template template = new Template(new File(config.getTemplatesDirectory(), templateFile), inputParameter);

        URI uri;
        if (addToMainView) {
            // uri with parent view
            uri = generateURI(JENKINS_BASE_PATH + VIEW_PATH + inputParameter.getParentView() + VIEW_PATH
                    + inputParameter.getProjectName() + "/createItem", "name=" + jobName);
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
            Header[] headers = response.getHeaders(RESPONSE_ERROR_HEADER);
            if (headers.length > 0) {
                LOGGER.warn("Unable to create job [" + inputParameter.getProjectName() + "]: " + headers[0]);
            } else {
                LOGGER.warn(
                        "Unable to create job [" + inputParameter.getProjectName() + "]: " + response.getStatusLine());
            }
        }
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

            // get the jdk installation name
            Map<String, String> params = inputParameter.getParameterMap();
            if (params.containsKey(PARAM_JDK)) {
                String jdkVersion = params.get(PARAM_JDK);
                String slave = params.get(PARAM_SLAVE);

                JdkInstallation jdkInstallation = null;
                for (JdkInstallation jdk : config.getJdkInstallations()) {
                    if (jdk.getVersion().equals(jdkVersion) && jdk.getSlaves().contains(slave)) {
                        jdkInstallation = jdk;
                        break;
                    }
                }

                params.put(PARAM_JDK_NAME, jdkInstallation == null ? "" : jdkInstallation.getName());
            }

            // Main view
            generateProjectView();

            // Main build job
            if (inputParameter.getCreateMainJob()) {
                String jobName = inputParameter.getProjectName() + config.getMainJobSuffix();
                createJenkinsJob(jobName, "template-main.xml", true);
            }

            // Sonar job
            if (inputParameter.getCreateSonarJob()) {
                String jobName = inputParameter.getProjectName() + config.getSonarJobSuffix();
                createJenkinsJob(jobName, "template-sonar.xml", true);
            }

            if (inputParameter.getCreateWhitesourceJob()) {
                String jobName = inputParameter.getProjectName() + config.getWhitesourceJobSuffix();
                createJenkinsJob(jobName, "template-whitesource.xml", true);
            }

            // System tests
            if (inputParameter.getCreateSystemTestJobs()) {

                /*
                 * Create System Test Build
                 */
                String jobNameStBuild = inputParameter.getProjectName() + config.getStBuildJobSuffix();
                createJenkinsJob(jobNameStBuild, "template-st-build.xml", false);

                // ST dataloader job
                String dataloadJob = inputParameter.getProjectName() + config.getStDataloaderJobSuffix();
                createJenkinsJob(dataloadJob, "template-st-dataloader.xml", false);

                // ST start server job
                String startServerJob = inputParameter.getProjectName() + config.getStStartServerJobSuffix();
                createJenkinsJob(startServerJob, "template-st-wl-startserver.xml", false);

                // ST deploy job
                String deployJob = inputParameter.getProjectName() + config.getStDeployJobSuffix();
                createJenkinsJob(deployJob, "template-st-wl-deploy.xml", false);

                // ST tests job
                String testJob = inputParameter.getProjectName() + config.getStTestsJobSuffix();
                createJenkinsJob(testJob, "template-st-tests.xml", false);

                // ST ui tests job
                String uiTestJob = inputParameter.getProjectName() + config.getStUITestsJobSuffix();
                createJenkinsJob(uiTestJob, "template-st-ui-tests.xml", false);

                // ST undeploy job
                String undeployJob = inputParameter.getProjectName() + config.getStUndeployJobSuffix();
                createJenkinsJob(undeployJob, "template-st-wl-undeploy.xml", false);

                // ST stop server job
                String stopServerJob = inputParameter.getProjectName() + config.getStStopServerJobSuffix();
                createJenkinsJob(stopServerJob, "template-st-wl-stopserver.xml", false);

                // ST dataloader job
                String systemTestSequenceJob = inputParameter.getProjectName()
                        + config.getStMultijobSequenceJobSuffix();
                createJenkinsJob(systemTestSequenceJob, "template-st-sequence.xml", true);

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
                JobCreator jenkinsTool = new JobCreator("install/config.xml", "install/input/"
                        + JOptionPane.showInputDialog("Please enter input-filename! e.g.: input-faurecia-st.xml"));
                jenkinsTool.createJobs();
            } else if (args.length == 2) {
                JobCreator jenkinsTool = new JobCreator(args[0], args[1]);
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