package de.fraunhofer.ids.framework.daps;

import de.fraunhofer.iais.eis.DynamicAttributeToken;

/**
 * Implementations of the DapsTokenProvider interface must implement a method that should return a valid JWT Daps token
 * in its String representation.
 */
public interface DapsTokenProvider {
    /**
     * Get the DAPS JWT Token from a DAPS and return its compact String representation
     *
     * @return the Daps Token of the Connector
     */
    String provideDapsToken() throws ConnectorMissingCertExtensionException, DapsConnectionException, DapsEmptyResponseException;

    /**
     * Return the DAPS JWT Token in infomodel {@link DynamicAttributeToken} representation
     *
     * @return DynamicAttributeToken from the DAPS JWT
     */
    DynamicAttributeToken getDAT() throws ConnectorMissingCertExtensionException, DapsConnectionException, DapsEmptyResponseException;
}