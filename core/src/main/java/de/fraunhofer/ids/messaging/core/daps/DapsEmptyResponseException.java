package de.fraunhofer.ids.messaging.core.daps;

/**
 * Exception which is thrown when the Response from the DAPS is empty.
 */
public class DapsEmptyResponseException extends DapsTokenManagerException {
    private static final long serialVersionUID = 42L;

    /**
     * For Throwing a EmptyDapsResponseException with a custom error message.
     *
     * @param message the error message to be included with the exception
     */
    public DapsEmptyResponseException(final String message) {
        super(message);
    }

}
