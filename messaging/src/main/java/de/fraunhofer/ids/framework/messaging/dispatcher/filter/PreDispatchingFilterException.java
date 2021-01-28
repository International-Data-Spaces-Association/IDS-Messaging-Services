package de.fraunhofer.ids.framework.messaging.dispatcher.filter;

/**
 * Exception that is thrown when an error occurs during preprocessing of incoming headers with a {@link PreDispatchingFilter}.
 */
public class PreDispatchingFilterException extends Exception {

    public PreDispatchingFilterException() {
        super();
    }

    public PreDispatchingFilterException( final String message ) {
        super(message);
    }

    public PreDispatchingFilterException( final String message, final Throwable cause ) {
        super(message, cause);
    }

    public PreDispatchingFilterException( final Throwable cause ) {
        super(cause);
    }

    protected PreDispatchingFilterException( final String message, final Throwable cause,
                                             final boolean enableSuppression, final boolean writableStackTrace ) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
