package de.fraunhofer.ids.framework.messaging.dispatcher.filter;

/**
 * Builder class for PreDispatchingFilterResults
 */
public class PreDispatchingFilterResultBuilder {
    private Throwable error;
    private boolean   success;
    private String    message;

    public PreDispatchingFilterResultBuilder withError( final Throwable error ) {
        this.error = error;
        return this;
    }

    public PreDispatchingFilterResultBuilder withSuccess( final boolean success ) {
        this.success = success;
        return this;
    }

    public PreDispatchingFilterResultBuilder withMessage( final String message ) {
        this.message = message;
        return this;
    }

    public PreDispatchingFilterResult build() {
        return new PreDispatchingFilterResult(error, success, message);
    }
}