package de.fraunhofer.ids.messaging.core.daps;

/**
 * Exception is thrown if errors happen in the DAPS service.
 */
public class DapsTokenManagerException extends Exception {
    private static final long serialVersionUID = 42L;

    /**
     * For Throwing a DapsException with a custom error message.
     *
     * @param message the error message to be included with the exception
     */
    public DapsTokenManagerException(final String message) {
        super(message);
    }
}
