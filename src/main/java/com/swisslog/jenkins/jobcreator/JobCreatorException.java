package com.swisslog.jenkins.jobcreator;

/**
 * The JobCreatorException covers all IO/URI etc. exceptions happening during job/view creation.
 */
public class JobCreatorException extends Exception {

    /**
     * Instantiates a new job creator exception.
     */
    public JobCreatorException() {
        super();
    }

    /**
     * Instantiates a new job creator exception.
     * 
     * @param message
     *            the message
     * @param cause
     *            the rootcause
     */
    public JobCreatorException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Instantiates a new job creator exception.
     * 
     * @param message
     *            the message
     */
    public JobCreatorException(String message) {
        super(message);
    }

    /**
     * Instantiates a new job creator exception.
     * 
     * @param cause
     *            the cause
     */
    public JobCreatorException(Throwable cause) {
        super(cause);
    }

}
