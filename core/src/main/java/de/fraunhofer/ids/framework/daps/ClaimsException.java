package de.fraunhofer.ids.framework.daps;

/**
 * Exception that gets thrown, if errors occur while validating a DAT token
 */
public class ClaimsException extends Exception {

    /**
     * For Throwing a ClaimsException with a custom error message
     *
     * @param message the error message to be included with the exception
     */
    public ClaimsException( final String message ) {
        super(message);
    }
}
