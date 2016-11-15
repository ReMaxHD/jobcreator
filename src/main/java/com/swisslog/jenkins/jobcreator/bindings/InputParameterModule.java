/*
 * -----------------------------------------------------------------------------
 * Application     : WM 6
 * Revision        : $Revision: 376093 $
 * Revision date   : $Date: 2016-10-22 02:45:35 +0200 (Sa, 22 Okt 2016) $
 * Last changed by : $Author: b2yeowo $
 * URL             : $URL: http://almscdc.swisslog.com/repo/SWPD/Development/WM6/trunk/Software/WM6/wm6-tools/jenkins-jobcreator/src/main/java/com/swisslog/jenkins/jobcreator/bindings/InputParameterModule.java $
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

public class InputParameterModule {

    public static final String TEMPLATES_GLASSFISH = "tmpl-st-gf";
    public static final String TEMPLATES_WEBLOGIC = "tmpl-st-wl";

    public static final String SEQUENCE_TEMPLATE_DEFAULT = "tmpl-st-sequence-6phases";

    String moduleNumber;
    boolean moduleConfigJcomEar;
    String templatePrefix;
    String templateConfig;
    String moduleSequenceTemplate;

    private Map<String, String> moduleParameterMap = new HashMap<String, String>();

    /**
     * Create a new InputParameterModule.
     *
     */
    public InputParameterModule() {
        super();
    }

    public String getModuleNumber() {
        return moduleNumber;
    }

    public void setModuleNumber(String moduleNumber) {
        this.moduleNumber = moduleNumber;
    }

    public String getTemplatePrefix() {
        return templatePrefix;
    }

    public void setTemplatePrefix(String templatePrefix) {
        this.templatePrefix = templatePrefix;
    }

    public String getTemplateConfig() {
        return templateConfig;
    }

    public void setTemplateConfig(String templateConfig) {
        this.templateConfig = templateConfig;
    }

    /**
     * @return the moduleParameterMap
     */
    public Map<String, String> getModuleParameterMap() {
        return moduleParameterMap;
    }

    /**
     * @param moduleParameterMap
     *            the moduleParameterMap to set
     */
    public void setModuleParameterMap(Map<String, String> moduleParameterMap) {
        this.moduleParameterMap = moduleParameterMap;
    }

    public String getModuleSequenceTemplate() {
        // use 6phases template as default
        if (moduleSequenceTemplate == null || moduleSequenceTemplate.length() == 0) {
            return SEQUENCE_TEMPLATE_DEFAULT;
        }
        return moduleSequenceTemplate;
    }

    public void setModuleSequenceTemplate(String moduleSequenceTemplate) {
        this.moduleSequenceTemplate = moduleSequenceTemplate;
    }

    public boolean isModuleConfigJcomEar() {
        return moduleConfigJcomEar;
    }

    public void setModuleConfigJcomEar(boolean moduleConfigJcomEar) {
        this.moduleConfigJcomEar = moduleConfigJcomEar;
    }

    public List<String> getModuleTemplates() {
        List<String> templates = new ArrayList<String>();

        // use glassfish as default
        if (StringUtils.isBlank(templatePrefix)) {
            templatePrefix = TEMPLATES_GLASSFISH;
        }

        if (StringUtils.isBlank(templateConfig)) {
            // handle default template config
            templates.add(templatePrefix + "-dataloader");
            templates.add(templatePrefix + "-startserver");
            templates.add(templatePrefix + (isModuleConfigJcomEar() ? "-deployapp-ear" : "-deployapp"));
            templates.add(templatePrefix + "-run-st");
            templates.add(templatePrefix + (isModuleConfigJcomEar() ? "-undeployapp-ear" : "-undeployapp"));
            templates.add(templatePrefix + "-stopserver");
        } else {

            // handle non default template config
            String[] templateConfigs = StringUtils.split(templateConfig);
            for (String templatePostfix : templateConfigs) {
                templates.add(templatePrefix + templatePostfix);
            }
        }

        return templates;
    }

    public String getJobSuffix(String template) {
        if (template.contains("-dataloader")) {
            return "Dataloader";
        } else if (template.contains("-configureplant")) {
            return "Configure Plant";
        } else if (template.contains("-startserver")) {
            return "Start Server";
        } else if (template.contains("-deployapp")) {
            return "Deploy Application";
        } else if (template.contains("-run-st")) {
            return "Run System Tests";
        } else if (template.contains("-undeployapp")) {
            return "Undeploy Application";
        } else if (template.contains("-stopserver")) {
            return "Stop Server";
        }
        return "";
    }

    public boolean isSystemTestJob(String template) {
        return template.contains("run-st");
    }
}
