package de.fraunhofer.ids.framework.messaging.dispatcher.filter;

import java.util.Objects;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

/**
 * Result that is returned by a PreDispatchingFilter (with information about why a message was accepted or rejected).
 */
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class PreDispatchingFilterResult {
    @Getter
    Throwable error;

    @Getter
    boolean   success;

    @Getter
    String    message;

    /**
     * Static method returning a builder.
     *
     * @return a builder instance for this class
     */
    public static PreDispatchingFilterResultBuilder builder() {
        return new PreDispatchingFilterResultBuilder();
    }

    /**
     * Static method for a default successResult.
     *
     * @return a PreDispatchingFilterResult where success = true, but with empty message
     */
    public static PreDispatchingFilterResult successResult() {
        return successResult("");
    }

    /**
     * Create a successResult with given message.
     *
     * @param message the message of the Result
     * @return a PreDispatchingFilterResult where success = true, with given message
     */
    public static PreDispatchingFilterResult successResult(final String message) {
        return new PreDispatchingFilterResult(null, true, message);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final var that = (PreDispatchingFilterResult) o;

        return isSuccess() == that.isSuccess()
               && Objects.equals(getError(), that.getError())
               && Objects.equals(getMessage(), that.getMessage());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getError(), isSuccess(), getMessage());
    }
}
