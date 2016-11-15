/*
 * -----------------------------------------------------------------------------
 * Application     : WM 6
 * Revision        : $Revision: 376093 $
 * Revision date   : $Date: 2016-10-22 02:45:35 +0200 (Sa, 22 Okt 2016) $
 * Last changed by : $Author: b2yeowo $
 * URL             : $URL: http://almscdc.swisslog.com/repo/SWPD/Development/WM6/trunk/Software/WM6/wm6-tools/jenkins-jobcreator/src/main/java/com/swisslog/jenkins/jobcreator/polarion/config/Config.java $
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
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.transform.stream.StreamSource;

import com.polarion.alm.ws.client.types.tracker.WorkItem;
import com.swisslog.jenkins.jobcreator.polarion.workitem.WorkItemException;
import com.swisslog.jenkins.jobcreator.polarion.workitem.WorkItemService;

/**
 * The JobCreatorConfig class is a binding to the jobcreator-config.xml
 */
@XmlRootElement(name = "polarion-integration-config")
@XmlAccessorType(XmlAccessType.FIELD)
public class Config {

    private static String configFile = "polarion-integration-config.xml";

    private String svnlookExe;
    private String jobcreatorConfig;
    private String inputXmlLocation;
    private String adminMailRecipients;
    private String mainMvnTargets;
    private String lockSufix;
    private String dataLoaderConfigurationSufix;

    @XmlElement(name = "stLock")
    List<STLock> stLocks = new ArrayList<>();

    @XmlElement(name = "country")
    List<Country> countries = new ArrayList<>();

    @XmlElement(name = "wm6Version")
    List<Wm6Version> wm6Versions = new ArrayList<>();

    @XmlTransient
    WorkItem workItem;

    @XmlTransient
    WorkItemService workItemService;

    /**
     * Load the job creator config from an xml file and replace parameters with the data from the given work item
     *
     * @param workItem
     *            the work item
     * @return the config
     * @throws JAXBException
     *             throws a JAXBException if the config could not be unmarshalled
     * @throws WorkItemException
     * @throws RemoteException
     */
    public static Config loadConfig(WorkItem workItem, WorkItemService workItemService) throws JAXBException,
    WorkItemException, RemoteException {
        Config config = loadWithoutWorkItem();
        config.workItem = workItem;
        config.workItemService = workItemService;
        return config;
    }

    /**
     * Load the job creator config from an xml file. Parameters won't be replaced without a work item.
     *
     * @return the config
     * @throws JAXBException
     *             throws a JAXBException if the config could not be unmarshalled
     */
    public static Config loadWithoutWorkItem() throws JAXBException {
        Unmarshaller unmarshaller = JAXBContext.newInstance(Config.class).createUnmarshaller();
        return unmarshaller.unmarshal(new StreamSource(configFile), Config.class).getValue();
    }

    /**
     * Get the config file name
     *
     * @return the name of the config file
     */
    public static String getConfigFile() {
        return configFile;
    }

    /**
     * Set the config file name
     *
     * @param configFile
     *            the name of the config file
     */
    public static void setConfigFile(String configFile) {
        Config.configFile = configFile;
    }

    /**
     * Get the value of the given config parameter.
     *
     * @param param
     *            the config parameter
     * @return the value
     * @throws WorkItemException
     */
    public String get(ConfigParameter param) throws WorkItemException {
        return param.getValue(this);
    }

    /**
     * Get the lock.
     *
     * @return
     * @throws WorkItemException
     */
    public String getLock() throws WorkItemException {
        return get(ConfigParameter.FULL_NAME) + lockSufix;
    }

    /**
     * Get the name of the dataloader configuration xml.
     *
     * @return
     * @throws WorkItemException
     */
    public String getDataLoaderConfiguration() throws WorkItemException {
        return get(ConfigParameter.PROJECT_NAME_WITHOUT_SPACES) + dataLoaderConfigurationSufix;
    }

    /**
     * Get the system test lock (each slave has its own lock).
     *
     * @return the system test lock
     * @throws RemoteException
     * @throws WorkItemException
     */
    public String getSystemTestLock() throws RemoteException, WorkItemException {
        String slave = getSlave();
        if (slave != null) {
            for (STLock stLock : stLocks) {
                if (slave.equals(stLock.getSlave())) {
                    return stLock.getName();
                }
            }
        }
        throw new IllegalStateException("There is no system test lock for slave " + slave);
    }

    /**
     * Get the slave (depends on the country).
     *
     * @return the slave
     * @throws RemoteException
     * @throws WorkItemException
     */
    public String getSlave() throws RemoteException, WorkItemException {
        Country country = getCountry();
        return country.getSlave();
    }

    /**
     * Get the parent view (e.g. "01 - Switzerland")
     *
     * @return the parent view
     */
    public String getParentView() throws RemoteException, WorkItemException {
        return getCountry().getParentView();
    }

    /**
     * Get the country.
     *
     * @return the country object
     */
    public Country getCountry() throws RemoteException, WorkItemException {
        String countryName = workItemService.getCountry(workItem);
        if (countryName != null) {
            for (Country country : countries) {
                if (countryName.equals(country.getName())) {
                    return country;
                }
            }
        }
        throw new IllegalStateException("There is no country with name " + countryName);
    }

    /**
     * Get the jdk version for the given wm6 version. The method searches for an exact match of the wm6 version. If
     * there is none, it searches for a weak match (e.g. "6.13.x" matches "6.13.0-Beta1").
     *
     * @param wm6Version
     *            the wm6Version
     * @return the jdk version
     * @throws WorkItemException
     * @throws RemoteException
     */
    public String getJdkName() throws RemoteException, WorkItemException {
        String wm6Version = workItemService.getWm6Version(workItem);

        Wm6Version weakMatch = null;
        for (Wm6Version version : wm6Versions) {
            if (version.getName().equals(wm6Version)) {
                return version.getJdk();
            } else if (weakMatch == null && version.getName().toLowerCase().endsWith("x")) {
                String name = version.getName().substring(0, version.getName().length() - 2);
                if (wm6Version.startsWith(name)) {
                    weakMatch = version;
                }
            }
        }

        if (weakMatch == null) {
            throw new IllegalStateException("There is no jdk version for wm6Version " + wm6Version);
        } else {
            return weakMatch.getJdk();
        }
    }

    /**
     * Get the path to the svnlook.exe file
     *
     * @return the path
     */
    public String getSvnlookExe() {
        return svnlookExe;
    }

    /**
     * Set the path to the svnlook.exe file
     *
     * @param the
     *            path the path to the svnlook.exe file
     */
    public void setSvnlookExe(String svnlookExe) {
        this.svnlookExe = svnlookExe;
    }

    /**
     * Get the path to the jobcreator config.
     *
     * @return the path
     */
    public String getJobcreatorConfig() {
        return jobcreatorConfig;
    }

    /**
     * Set the path to the jobcreator config.
     *
     * @param jobcreatorConfig
     *            the path
     */
    public void setJobcreatorConfig(String jobcreatorConfig) {
        this.jobcreatorConfig = jobcreatorConfig;
    }

    /**
     * Get the path to the folder where all input xml files should be saved.
     *
     * @return the path
     */
    public String getInputXmlLocation() {
        return inputXmlLocation;
    }

    /**
     * Set the path to the folder where all input xml files should be saved.
     *
     * @param inputXmlLocation
     */
    public void setInputXmlLocation(String inputXmlLocation) {
        this.inputXmlLocation = inputXmlLocation;
    }

    public String getAdminMailRecipients() {
        return adminMailRecipients;
    }

    public void setAdminMailRecipients(String adminMailRecipients) {
        this.adminMailRecipients = adminMailRecipients;
    }

    public String getMainMvnTargets() {
        return mainMvnTargets;
    }

    public void setMainMvnTargets(String mainMvnTargets) {
        this.mainMvnTargets = mainMvnTargets;
    }

    public String getLockSufix() {
        return lockSufix;
    }

    public void setLockSufix(String lockSufix) {
        this.lockSufix = lockSufix;
    }

    public String getDataLoaderConfigurationSufix() {
        return dataLoaderConfigurationSufix;
    }

    public void setDataLoaderConfigurationSufix(String dataLoaderConfigurationSufix) {
        this.dataLoaderConfigurationSufix = dataLoaderConfigurationSufix;
    }

    /**
     * Get the work item
     *
     * @return the work item
     */
    public WorkItem getWorkItem() {
        return workItem;
    }

    /**
     * Get the work item service
     *
     * @return the work item service
     */
    public WorkItemService getWorkItemService() {
        return workItemService;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((adminMailRecipients == null) ? 0 : adminMailRecipients.hashCode());
        result = prime * result + ((countries == null) ? 0 : countries.hashCode());
        result = prime * result + ((inputXmlLocation == null) ? 0 : inputXmlLocation.hashCode());
        result = prime * result + ((jobcreatorConfig == null) ? 0 : jobcreatorConfig.hashCode());
        result = prime * result + ((lockSufix == null) ? 0 : lockSufix.hashCode());
        result = prime * result
                + ((dataLoaderConfigurationSufix == null) ? 0 : dataLoaderConfigurationSufix.hashCode());
        result = prime * result + ((mainMvnTargets == null) ? 0 : mainMvnTargets.hashCode());
        result = prime * result + ((stLocks == null) ? 0 : stLocks.hashCode());
        result = prime * result + ((svnlookExe == null) ? 0 : svnlookExe.hashCode());
        result = prime * result + ((wm6Versions == null) ? 0 : wm6Versions.hashCode());
        result = prime * result + ((workItem == null) ? 0 : workItem.hashCode());
        result = prime * result + ((workItemService == null) ? 0 : workItemService.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Config other = (Config) obj;
        if (adminMailRecipients == null) {
            if (other.adminMailRecipients != null) {
                return false;
            }
        } else if (!adminMailRecipients.equals(other.adminMailRecipients)) {
            return false;
        }
        if (countries == null) {
            if (other.countries != null) {
                return false;
            }
        } else if (!countries.equals(other.countries)) {
            return false;
        }
        if (inputXmlLocation == null) {
            if (other.inputXmlLocation != null) {
                return false;
            }
        } else if (!inputXmlLocation.equals(other.inputXmlLocation)) {
            return false;
        }
        if (jobcreatorConfig == null) {
            if (other.jobcreatorConfig != null) {
                return false;
            }
        } else if (!jobcreatorConfig.equals(other.jobcreatorConfig)) {
            return false;
        }
        if (lockSufix == null) {
            if (other.lockSufix != null) {
                return false;
            }
        } else if (!lockSufix.equals(other.lockSufix)) {
            return false;
        }
        if (dataLoaderConfigurationSufix == null) {
            if (other.dataLoaderConfigurationSufix != null) {
                return false;
            }
        } else if (!dataLoaderConfigurationSufix.equals(other.dataLoaderConfigurationSufix)) {
            return false;
        }
        if (mainMvnTargets == null) {
            if (other.mainMvnTargets != null) {
                return false;
            }
        } else if (!mainMvnTargets.equals(other.mainMvnTargets)) {
            return false;
        }
        if (stLocks == null) {
            if (other.stLocks != null) {
                return false;
            }
        } else if (!stLocks.equals(other.stLocks)) {
            return false;
        }
        if (svnlookExe == null) {
            if (other.svnlookExe != null) {
                return false;
            }
        } else if (!svnlookExe.equals(other.svnlookExe)) {
            return false;
        }
        if (wm6Versions == null) {
            if (other.wm6Versions != null) {
                return false;
            }
        } else if (!wm6Versions.equals(other.wm6Versions)) {
            return false;
        }
        if (workItem == null) {
            if (other.workItem != null) {
                return false;
            }
        } else if (!workItem.equals(other.workItem)) {
            return false;
        }
        if (workItemService == null) {
            if (other.workItemService != null) {
                return false;
            }
        } else if (!workItemService.equals(other.workItemService)) {
            return false;
        }
        return true;
    }
}