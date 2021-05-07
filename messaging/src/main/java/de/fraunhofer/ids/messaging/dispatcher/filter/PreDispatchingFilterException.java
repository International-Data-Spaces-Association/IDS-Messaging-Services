package de.fraunhofer.ids.messaging.dispatcher.filter;

/**
 * Exception that is thrown when an error occurs during preprocessing of incoming headers with a {@link PreDispatchingFilter}.
 */
public class PreDispatchingFilterException extends Exception {
    private static final long serialVersionUID = 42L;

    /**
     * Exception that is thrown when an error occurs during preprocessing of incoming headers.
     */
    public PreDispatchingFilterException() {
        super();
    }

    /**
     * Exception that is thrown when an error occurs during preprocessing of incoming headers.
     *
     * @param message Message to be thrown
     */
    public PreDispatchingFilterException(final String message) {
        super(message);
    }

    /**
     * Exception that is thrown when an error occurs during preprocessing of incoming headers.
     *
     * @param message Message to be thrown
     * @param cause   Cause of Exception
     */
    public PreDispatchingFilterException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Exception that is thrown when an error occurs during preprocessing of incoming headers.
     *
     * @param cause Cause of Exception
     */
    public PreDispatchingFilterException(final Throwable cause) {
        super(cause);
    }

    protected PreDispatchingFilterException(final String message,
                                            final Throwable cause,
                                            final boolean enableSuppression,
                                            final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
