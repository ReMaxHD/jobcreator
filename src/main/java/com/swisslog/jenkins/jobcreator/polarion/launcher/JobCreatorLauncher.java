/*
 * -----------------------------------------------------------------------------
 * Application     : WM 6
 * Revision        : $Revision: 376093 $
 * Revision date   : $Date: 2016-10-22 02:45:35 +0200 (Sa, 22 Okt 2016) $
 * Last changed by : $Author: b2yeowo $
 * URL             : $URL: http://almscdc.swisslog.com/repo/SWPD/Development/WM6/trunk/Software/WM6/wm6-tools/jenkins-jobcreator/src/main/java/com/swisslog/jenkins/jobcreator/polarion/launcher/JobCreatorLauncher.java $
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
package com.swisslog.jenkins.jobcreator.polarion.launcher;

import java.io.IOException;

import com.swisslog.jenkins.jobcreator.polarion.config.Config;

/**
 * Use this interface to start the jenkins-jobcreator
 */
public interface JobCreatorLauncher {

    /**
     * Start the jenkins-jobcreator jar and passe the input.xml file name as argument
     * 
     * @param config
     *            the config (contains the jar and config locations)
     * @param inputFileName
     *            the input.xml file name
     * @throws IOException
     */
    void launchJobCreator(Config config, String inputFileName) throws IOException;
}