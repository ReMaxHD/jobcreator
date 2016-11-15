/*
 * -----------------------------------------------------------------------------
 * Application     : WM 6
 * Revision        : $Revision: 376093 $
 * Revision date   : $Date: 2016-10-22 02:45:35 +0200 (Sa, 22 Okt 2016) $
 * Last changed by : $Author: b2yeowo $
 * URL             : $URL: http://almscdc.swisslog.com/repo/SWPD/Development/WM6/trunk/Software/WM6/wm6-tools/jenkins-jobcreator/src/main/java/com/swisslog/jenkins/jobcreator/bindings/JdkInstallation.java $
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
package com.swisslog.jenkins.jobcreator.bindings;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This class is used to get the jdk installation name by the java version and slave
 */
@XmlRootElement(name = "jdkInstallation")
@XmlAccessorType(XmlAccessType.FIELD)
public class JdkInstallation {

    private String name;
    private String version;

    @XmlElement(name = "slave")
    private List<String> slaves = null;

    /**
     * Get the jdk installation name
     * 
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Set the jdk installation name
     * 
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the java version
     * 
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Set the java version
     * 
     * @param version
     *            the version
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Get the slaves on which this jdk can be used
     * 
     * @return the slaves
     */
    public List<String> getSlaves() {
        return slaves;
    }

    /**
     * Set the slaves on which this jdk can be used
     * 
     * @param slaves
     *            the slaves
     */
    public void setSlaves(List<String> slaves) {
        this.slaves = slaves;
    }

}