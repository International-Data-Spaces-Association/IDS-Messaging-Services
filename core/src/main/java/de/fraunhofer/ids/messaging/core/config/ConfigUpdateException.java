package de.fraunhofer.ids.messaging.core.config;

/**
 * Exception that is thrown, when an error occurs while trying to change the configuration.
 * using {@link ConfigContainer}
 */
public class ConfigUpdateException extends Exception {
    private static final long serialVersionUID = 42L;

    /**
     * Create a ConfigurationUpdateException with a given Message and Cause.
     *
     * @param message error message of the exception
     * @param cause   cause for the exception
     */
    public ConfigUpdateException(final String message, final Throwable cause) {
        super(message, cause);
    }
}