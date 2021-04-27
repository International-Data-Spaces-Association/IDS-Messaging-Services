package de.fraunhofer.ids.framework.config.ssl.keystore;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

/**
 * Exception which is thrown, when the {@link KeyStoreManager} cannot be initialized.
 */
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class KeyStoreManagerInitializationException extends Exception {
    static long serialVersionUID = 42L;

    /**
     * Create a KeyStoreManagerInitializationException with a given Message and Cause.
     *
     * @param message error message of the exception
     * @param cause   cause for the exception
     */
    public KeyStoreManagerInitializationException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
