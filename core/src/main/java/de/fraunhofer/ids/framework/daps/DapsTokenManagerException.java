package de.fraunhofer.ids.framework.daps;

/**
 * Exception is thrown if errors happen in the DAPS service
 */
public class DapsTokenManagerException extends Exception {
    /**
     * For Throwing a DapsException with a custom error message
     *
     * @param message the error message to be included with the exception
     */
    public DapsTokenManagerException( String message ) {
        super(message);
    }
}
