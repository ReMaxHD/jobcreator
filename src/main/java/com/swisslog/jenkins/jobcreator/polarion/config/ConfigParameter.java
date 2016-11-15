/*
 * -----------------------------------------------------------------------------
 * Application     : WM 6
 * Revision        : $Revision: 376093 $
 * Revision date   : $Date: 2016-10-22 02:45:35 +0200 (Sa, 22 Okt 2016) $
 * Last changed by : $Author: b2yeowo $
 * URL             : $URL: http://almscdc.swisslog.com/repo/SWPD/Development/WM6/trunk/Software/WM6/wm6-tools/jenkins-jobcreator/src/main/java/com/swisslog/jenkins/jobcreator/polarion/config/ConfigParameter.java $
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

import com.swisslog.jenkins.jobcreator.polarion.workitem.WorkItemException;

/**
 * Config parameters can be used in the jobcreator-config.xml. The name (e.g. %PROJECT_NAME%) will be replaced with its
 * value.
 */
public enum ConfigParameter {
    PROJECT_NAME() {
        @Override
        public String getValue(Config config) throws WorkItemException {
            try {
                return config.getWorkItemService().getProjectName(config.getWorkItem());
            } catch (RemoteException e) {
                throw new WorkItemException("Could not get project name from work item due to RemoteException", e);
            }
        }
    },
    PROJECT_NAME_WITHOUT_SPACES() {
        @Override
        public String getValue(Config config) throws WorkItemException {
            return PROJECT_NAME.getValue(config).replaceAll(" ", "");
        }
    },
    PROJECT_NAME_LOWER_CASE() {
        @Override
        public String getValue(Config config) throws WorkItemException {
            return PROJECT_NAME.getValue(config).replaceAll(" ", "").toLowerCase();
        }
    },
    PROJECT_NAME_UNDERSCORE() {
        @Override
        public String getValue(Config config) throws WorkItemException {
            return PROJECT_NAME.getValue(config).replaceAll(" ", "_");
        }
    },
    BUILD_LOCATION() {
        @Override
        public String getValue(Config config) throws WorkItemException {
            return "D:\\jenkins\\workspace\\" + PROJECT_NAME_UNDERSCORE.getValue(config) + "_-_Build";
        }
    },
    WAR() {
        @Override
        public String getValue(Config config) throws WorkItemException {
            return "wm6-" + PROJECT_NAME_LOWER_CASE.getValue(config) + "-war";
        }
    },
    INSTALLATION_SITE() {
        @Override
        public String getValue(Config config) throws WorkItemException {
            try {
                String site = config.getWorkItemService().getInstallationSite(config.getWorkItem());
                if (site == null) {
                    site = "";
                }
                return site;
            } catch (RemoteException e) {
                throw new WorkItemException("Could not get installation site from work item due to RemoteException", e);
            }
        }
    },
    FULL_NAME() {
        @Override
        public String getValue(Config config) throws WorkItemException {
            String name = PROJECT_NAME.getValue(config);
            String site = INSTALLATION_SITE.getValue(config);
            if (site != null && !site.isEmpty()) {
                name += " - " + site;
            }
            return name;
        }
    },
    SONAR_BRANCH() {
        @Override
        public String getValue(Config config) throws WorkItemException {
            String name = PROJECT_NAME_WITHOUT_SPACES.getValue(config);
            name += INSTALLATION_SITE.getValue(config).replaceAll(" ", "");
            return name;
        }
    },
    FULL_NAME_LOWER_CASE() {
        @Override
        public String getValue(Config config) throws WorkItemException {
            String name = PROJECT_NAME_LOWER_CASE.getValue(config);
            name += INSTALLATION_SITE.getValue(config).replaceAll(" ", "").toLowerCase();
            return name;
        };
    };

    /**
     * Get the parameter name (can be used in xml file and will be replaced with its value)
     *
     * @return the parameter name
     */
    public String getName() {
        return "%" + name() + "%";
    }

    /**
     * Get the value for the given config
     *
     * @param config
     *            the config
     * @return the value
     * @throws WorkItemException
     *             throws an WorkItemException if data could not be extracted from work item
     */
    public abstract String getValue(Config config) throws WorkItemException;
}