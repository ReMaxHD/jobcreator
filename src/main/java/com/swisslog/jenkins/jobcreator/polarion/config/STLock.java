/*
 * -----------------------------------------------------------------------------
 * Application     : WM 6
 * Revision        : $Revision: 376093 $
 * Revision date   : $Date: 2016-10-22 02:45:35 +0200 (Sa, 22 Okt 2016) $
 * Last changed by : $Author: b2yeowo $
 * URL             : $URL: http://almscdc.swisslog.com/repo/SWPD/Development/WM6/trunk/Software/WM6/wm6-tools/jenkins-jobcreator/src/main/java/com/swisslog/jenkins/jobcreator/polarion/config/STLock.java $
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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 * This class is a simple binding for a wm6Version tag in the jobcreator-config.xml
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class STLock {
    private String name;
    private String slave;

    public STLock() {
        super();
    }

    public STLock(String name, String slave) {
        super();
        this.name = name;
        this.slave = slave;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlave() {
        return slave;
    }

    public void setSlave(String slave) {
        this.slave = slave;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((slave == null) ? 0 : slave.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
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
        STLock other = (STLock) obj;
        if (slave == null) {
            if (other.slave != null) {
                return false;
            }
        } else if (!slave.equals(other.slave)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }
}