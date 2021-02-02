package de.fraunhofer.ids.framework.config;

/**
 * Exception that is thrown, when an error occurs while trying to change the configuration
 * using {@link ConfigContainer}
 */
public class ConfigUpdateException extends Exception {

    /**
     * Create a ConfigurationUpdateException with a given Message and Cause
     *
     * @param message error message of the exception
     * @param cause   cause for the exception
     */
    public ConfigUpdateException( String message, Throwable cause ) {
        super(message, cause);
    }
}
