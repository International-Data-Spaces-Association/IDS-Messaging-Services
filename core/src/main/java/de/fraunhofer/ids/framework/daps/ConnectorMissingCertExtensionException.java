package de.fraunhofer.ids.framework.daps;

/**
 * Exception that is thrown, when an Extension (aki, ski) of the cert is missing.
 */
public class ConnectorMissingCertExtensionException extends DapsTokenManagerException {
    private static final long serialVersionUID = 42L;

    /**
     * For Throwing a MissingCertExtensionException with a custom error message.
     *
     * @param message the error message to be included with the exception
     */
    public ConnectorMissingCertExtensionException(final String message) {
        super(message);
    }

}
