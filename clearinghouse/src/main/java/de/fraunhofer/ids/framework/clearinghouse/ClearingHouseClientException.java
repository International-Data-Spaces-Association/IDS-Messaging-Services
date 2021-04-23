package de.fraunhofer.ids.framework.clearinghouse;

public class ClearingHouseClientException extends Exception {
    private static final long serialVersionUID = 42L;

    /**
     * Exception which can be thrown while processing sending a log to the Clearing-House.
     *
     * @param message The message (String) for the exception
     * @param cause   The actual exception
     */
    public ClearingHouseClientException(final String message, final Exception cause) {
        super(message, cause);
    }
}
