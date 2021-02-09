package de.fraunhofer.ids.framework.daps;

/**
 * Exception is thrown if errors happen in the DAPS service
 */
public class DapsConnectionException extends DapsTokenManagerException {
    /**
     * Exception is thrown if communication to the DAPS fails. For example, if the DAPS URL is incorrect or other connection problems to the DAPS occur.
     *
     * @param message the error message to be included with the exception
     */
    public DapsConnectionException( String message ) {
        super(message);
    }
}
