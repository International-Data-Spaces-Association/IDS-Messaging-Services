package de.fraunhofer.ids.framework.daps;

public interface TokenManagerService {

    /**
     * Acuire the DAPS DAT
     * @param dapsURL The URL of the DAPS
     * @return The DAT as String
     * @throws DapsConnectionException Thrown if no connection to DAPS possible
     * @throws DapsEmptyResponseException Thrown if DAPS returned invalid response
     * @throws ConnectorMissingCertExtensionException Thrown if something is wrong with the Connector Certificate
     */
    String acquireToken( String dapsURL )
            throws DapsConnectionException, DapsEmptyResponseException, ConnectorMissingCertExtensionException;
}
