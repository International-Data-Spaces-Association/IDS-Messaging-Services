package de.fraunhofer.ids.framework.clearinghouse;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ClearingHouseClientException extends Exception {
    static long serialVersionUID = 42L;

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
