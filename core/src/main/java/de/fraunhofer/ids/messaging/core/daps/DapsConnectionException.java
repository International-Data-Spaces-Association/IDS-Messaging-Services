package de.fraunhofer.ids.messaging.core.daps;

/**
 * Exception is thrown if errors happen in the DAPS service.
 */
public class DapsConnectionException extends DapsTokenManagerException {
    private static final long serialVersionUID = 42L;

    /**
     * Exception is thrown if communication to the DAPS fails. For example, if the DAPS URL is incorrect or other connection problems to the DAPS occur.
     *
     * @param message the error message to be included with the exception
     */
    public DapsConnectionException(final String message) {
        super(message);
    }
}
