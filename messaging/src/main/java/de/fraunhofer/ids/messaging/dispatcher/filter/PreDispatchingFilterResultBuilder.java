package de.fraunhofer.ids.messaging.dispatcher.filter;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

/**
 * Builder class for PreDispatchingFilterResults.
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PreDispatchingFilterResultBuilder {
    Throwable error;
    boolean   success;
    String    message;

    /**
     * Error message of the PreDispatchingFilter.
     *
     * @param error the throwable error
     * @return PreDispatchingFilterResultBuilder
     */
    public PreDispatchingFilterResultBuilder withError(final Throwable error) {
        this.error = error;
        return this;
    }

    /**
     * Success indicator of the PreDispatchingFilter.
     *
     * @param success boolean if filter was successfull
     * @return PreDispatchingFilterResultBuilder
     */
    public PreDispatchingFilterResultBuilder withSuccess(final boolean success) {
        this.success = success;
        return this;
    }

    /**
     * Message of the PreDispatchingFilter.
     *
     * @param message Message of the PreDispatchingFilter
     * @return PreDispatchingFilterResultBuilder
     */
    public PreDispatchingFilterResultBuilder withMessage(final String message) {
        this.message = message;
        return this;
    }

    /**
     * Build the PreDispatchingFilter-Result.
     *
     * @return The build PreDispatchingFilter-Result
     */
    public PreDispatchingFilterResult build() {
        return new PreDispatchingFilterResult(error, success, message);
    }
}