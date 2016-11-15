/*
 * -----------------------------------------------------------------------------
 * Application     : WM 6
 * Revision        : $Revision: 376093 $
 * Revision date   : $Date: 2016-10-22 02:45:35 +0200 (Sa, 22 Okt 2016) $
 * Last changed by : $Author: b2yeowo $
 * URL             : $URL: http://almscdc.swisslog.com/repo/SWPD/Development/WM6/trunk/Software/WM6/wm6-tools/jenkins-jobcreator/src/main/java/com/swisslog/jenkins/jobcreator/polarion/workitem/WorkItemServiceImpl.java $
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

import org.apache.log4j.Logger;

import com.polarion.alm.ws.client.tracker.TrackerWebService;
import com.polarion.alm.ws.client.types.projects.User;
import com.polarion.alm.ws.client.types.tracker.Custom;
import com.polarion.alm.ws.client.types.tracker.EnumOption;
import com.polarion.alm.ws.client.types.tracker.EnumOptionId;
import com.polarion.alm.ws.client.types.tracker.LinkedWorkItem;
import com.polarion.alm.ws.client.types.tracker.WorkItem;

public class WorkItemServiceImpl implements WorkItemService {

    public static final String SVN_REPOSITORY = "svnRepository";
    public static final String CUSTOMER_PROJECT = "customerProject";
    public static final String MAIL_RECIPIENTS = "mailRecipients";
    public static final String INSTALLATION_SITE = "site";
    public static final String COUNTRY = "country";
    public static final String WM6_VERSION = "release";
    public static final String CUSTOMER_PROJECT_WORK_ITEM_TYPE = "customer";
    public static final String CUSTOMER_ENUM_ID = "customer";

    private static final Logger LOGGER = Logger.getLogger(WorkItemServiceImpl.class);

    TrackerWebService trackerService;

    private WorkItem cachedProjectWorkItem;

    /**
     * Create a new WorkItemService.
     *
     * @param trackerService
     *            the tracker service that should be used to get linked work items
     */
    public WorkItemServiceImpl(TrackerWebService trackerService) {
        super();
        this.trackerService = trackerService;
    }

    /**
     * Get a custom field from the work item
     *
     * @param workItem
     *            the work item
     * @param fieldName
     *            the name of the custom field
     * @return the value of the custom field
     */
    private Object getCustomField(WorkItem workItem, String fieldName) {
        for (Custom field : workItem.getCustomFields()) {
            if (field.getKey().equals(fieldName)) {
                return field.getValue();
            }
        }
        return null;
    }

    @Override
    public String getProjectName(WorkItem workItem) throws WorkItemException, RemoteException {
        EnumOptionId projectOptionId = (EnumOptionId) getCustomField(workItem, CUSTOMER_PROJECT);
        if (projectOptionId == null) {
            throw new WorkItemException("Could not get value of field " + CUSTOMER_PROJECT + " from work item");
        }
        EnumOption projectOption = trackerService.getEnumOptionWithEnumId(workItem.getUri(), CUSTOMER_ENUM_ID,
                projectOptionId);
        if (projectOption == null) {
            throw new WorkItemException("Could not get value of field " + CUSTOMER_PROJECT + " from work item");
        } else {
            return projectOption.getName();
        }
    }

    /**
     * Get the customer project work item
     *
     * @param workItem
     *            the work item (central tools or system tests)
     * @return the customer project work item
     * @throws WorkItemException
     *             throws a WorkItemException if the project work item could not be extracted from the work item
     * @throws RemoteException
     *             throws a RemoteException if connection to polarion failed
     */
    private WorkItem getProjectWorkItem(WorkItem workItem) throws WorkItemException, RemoteException {
        if (cachedProjectWorkItem != null) {
            return cachedProjectWorkItem;
        }

        LinkedWorkItem[] linkedWorkItems = workItem.getLinkedWorkItems();

        LOGGER.info("Searchig linked work items ...");
        for (LinkedWorkItem linkedWorkItem : linkedWorkItems) {
            WorkItem item = trackerService.getWorkItemByUri(linkedWorkItem.getWorkItemURI());

            String type = item.getType() == null ? null : item.getType().getId();
            LOGGER.info("Found linked work item of type " + type + " with uri " + item.getUri());
            if (CUSTOMER_PROJECT_WORK_ITEM_TYPE.equals(type)) {
                cachedProjectWorkItem = item;
                return item;
            }
        }

        throw new WorkItemException("Could not get linked customer project work item");
    }

    @Override
    public String getInstallationSite(WorkItem workItem) throws WorkItemException, RemoteException {
        return (String) getCustomField(getProjectWorkItem(workItem), INSTALLATION_SITE);
    }

    @Override
    public String getCountry(WorkItem workItem) throws WorkItemException, RemoteException {
        EnumOptionId countryOption = (EnumOptionId) getCustomField(getProjectWorkItem(workItem), COUNTRY);
        if (countryOption == null) {
            throw new WorkItemException("Could not get value of field " + COUNTRY + " from work item");
        }
        return countryOption.getId();
    }

    @Override
    public String getWm6Version(WorkItem workItem) throws WorkItemException, RemoteException {
        WorkItem projectWorkItem = getProjectWorkItem(workItem);
        EnumOptionId option = (EnumOptionId) getCustomField(projectWorkItem, WM6_VERSION);
        if (option == null) {
            throw new WorkItemException("Could not get value of field " + WM6_VERSION + " from work item");
        }
        return option.getId();
    }

    @Override
    public String getSvnUrl(WorkItem workItem) throws WorkItemException {
        String svnUrl = (String) getCustomField(workItem, SVN_REPOSITORY);
        if (svnUrl == null || svnUrl.equals("")) {
            throw new WorkItemException("Could not get value of field " + SVN_REPOSITORY + " from work item");
        }
        return svnUrl;
    }

    @Override
    public String getAuthorMail(WorkItem workItem) throws WorkItemException {
        User author = workItem.getAuthor();
        if (author == null) {
            throw new WorkItemException("Could not get author of work item");
        }
        return author.getEmail();
    }
}