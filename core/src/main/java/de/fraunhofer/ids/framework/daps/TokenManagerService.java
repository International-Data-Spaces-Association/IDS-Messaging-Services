package de.fraunhofer.ids.framework.daps;

public interface TokenManagerService {

    String acquireToken(String dapsURL) throws DapsConnectionException, DapsEmptyResponseException, ConnectorMissingCertExtensionException;

}
