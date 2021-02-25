package de.fraunhofer.ids.framework.daps;

import java.security.Key;

/**
 * an Implementation of this has to provide the Public Key from the DAPS Service
 */
public interface DapsPublicKeyProvider {

    /**
     * Get the Public Key from the JWKS of the DAPS
     *
     * @return the public Key of a DAPS Service
     */
    Key providePublicKey();
}
