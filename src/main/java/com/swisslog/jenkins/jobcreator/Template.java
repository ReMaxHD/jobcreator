package com.swisslog.jenkins.jobcreator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Scanner;

import com.swisslog.jenkins.jobcreator.bindings.InputParameter;

/**
 * The Template loads a jenkins template file, and does replace all given paramter of an InputMap.
 */
public class Template {

    /** The patched XML to be posted as view/job */
    String templateContent;

    /**
     * Instantiates a new template, loads the XML into a String and replaces all given input parameter if possible.
     *
     * @param file
     *            the template file
     * @param inputParameter
     *            the input parameters
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public Template(File file, InputParameter inputParameter) throws IOException {

        FileInputStream fis = new FileInputStream(file);
        @SuppressWarnings("resource")
        Scanner scanner = new Scanner(fis, "UTF-8").useDelimiter("\\A");
        this.templateContent = scanner.next();
        scanner.close();

        // Replace all input parameters
        for (String key : inputParameter.getParameterMap().keySet()) {
            this.templateContent = templateContent.replace(key, inputParameter.getParameterMap().get(key));
        }

    }

    /**
     * Returns a prepared template
     *
     * @return the template
     */
    public String getTemplateContent() {
        return templateContent;
    }

}
