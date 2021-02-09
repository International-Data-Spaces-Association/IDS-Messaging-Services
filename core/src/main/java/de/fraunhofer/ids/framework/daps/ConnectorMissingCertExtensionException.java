package de.fraunhofer.ids.framework.daps;

/**
 * Exception that is thrown, when an Extension (aki, ski) of the cert is missing
 */
public class ConnectorMissingCertExtensionException extends DapsTokenManagerException {

    /**
     * For Throwing a MissingCertExtensionException with a custom error message
     *
     * @param message the error message to be included with the exception
     */
    public ConnectorMissingCertExtensionException( String message) {
        super(message);
    }

}
