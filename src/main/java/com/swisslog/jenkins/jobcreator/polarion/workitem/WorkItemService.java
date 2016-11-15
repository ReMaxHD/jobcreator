/*
 * -----------------------------------------------------------------------------
 * Application     : WM 6
 * Revision        : $Revision: 376093 $
 * Revision date   : $Date: 2016-10-22 02:45:35 +0200 (Sa, 22 Okt 2016) $
 * Last changed by : $Author: b2yeowo $
 * URL             : $URL: http://almscdc.swisslog.com/repo/SWPD/Development/WM6/trunk/Software/WM6/wm6-tools/jenkins-jobcreator/src/main/java/com/swisslog/jenkins/jobcreator/polarion/workitem/WorkItemService.java $
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

import com.polarion.alm.ws.client.types.tracker.WorkItem;

/**
 * Use this class to extract data from a work item
 */
public interface WorkItemService {

    /**
     * Get the project name
     *
     * @param workItem
     *            the work item (central tools or system tests)
     * @return the project name
     * @throws WorkItemException
     *             throws a WorkItemException if the project name could not be extracted from the work item
     * @throws RemoteException
     *             throws a RemoteException if connection to polarion failed
     */
    String getProjectName(WorkItem workItem) throws WorkItemException, RemoteException;

    /**
     * Get installation site
     *
     * @param workItem
     *            the work item (central tools or system tests)
     * @return the installation site
     * @throws WorkItemException
     *             throws a WorkItemException if the project name could not be extracted from the work item
     * @throws RemoteException
     *             throws a RemoteException if connection to polarion failed
     */
    String getInstallationSite(WorkItem workItem) throws WorkItemException, RemoteException;

    /**
     * Get the country (from the linked customer project work item)
     *
     * @param workItem
     *            the work item (central tools or system tests)
     * @return the country
     * @throws WorkItemException
     *             throws a WorkItemException if the country could not be extracted from the work item
     * @throws RemoteException
     *             throws a RemoteException if connection to polarion failed
     */
    String getCountry(WorkItem workItem) throws WorkItemException, RemoteException;

    /**
     * Get the wm6 version (from the linked customer project work item)
     *
     * @param workItem
     *            the work item (central tools or system tests)
     * @return the wm6version
     * @throws WorkItemException
     *             throws a WorkItemException if the wm6 version could not be extracted from the work item
     * @throws RemoteException
     *             throws a RemoteException if connection to polarion failed
     */
    String getWm6Version(WorkItem workItem) throws WorkItemException, RemoteException;

    /**
     * Get the svn url
     *
     * @param workItem
     *            the work item (central tools)
     * @return the svn url
     * @throws WorkItemException
     *             throws a WorkItemException if the svn url could not be extracted from the work item
     */
    String getSvnUrl(WorkItem workItem) throws WorkItemException;

    /**
     * Get the author's mail address
     *
     * @param workItem
     *            the work item (central tools or system test)
     * @return the mail address
     * @throws WorkItemException
     *             throws a WorkItemException if the author's mail address could not be extracted from the work item
     */
    String getAuthorMail(WorkItem workItem) throws WorkItemException;
}