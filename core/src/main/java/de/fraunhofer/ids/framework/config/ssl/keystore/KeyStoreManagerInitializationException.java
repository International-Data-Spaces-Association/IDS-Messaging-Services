package de.fraunhofer.ids.framework.config.ssl.keystore;

/**
 * Exception which is thrown, when the {@link KeyStoreManager} cannot be initialized.
 */
public class KeyStoreManagerInitializationException extends Exception {
    private static final long serialVersionUID = 42L;

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
