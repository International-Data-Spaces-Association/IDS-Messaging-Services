package de.fraunhofer.ids.messaging.core.daps;

import java.security.Key;
import java.util.List;

/**
 * an Implementation of this has to provide the Public Key from the DAPS Service.
 */
public interface DapsPublicKeyProvider {

    /**
     * Get the Public Key from the JWKS of the DAPS.
     *
     * @return the public Key of a DAPS Service
     */
    List<Key> providePublicKeys();
}