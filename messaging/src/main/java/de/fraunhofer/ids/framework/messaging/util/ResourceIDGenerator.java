package de.fraunhofer.ids.framework.messaging.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

/**
 * Utility for generating Resource IDs for infomodel builders
 */
public final class ResourceIDGenerator {
    private static final String URI_BASE = "https://w3id.org/idsa/autogen";

    private ResourceIDGenerator() { }

    /**
     * Create an URI with callerClazz name and random uuid in path (used as ID URIs)
     *
     * @param callerClazz class for which the randomURI should be generated
     *
     * @return a random URI ID
     */
    public static URI randomURI( final Class<?> callerClazz ) {
        try {
            return new URI(String.format("%s/%s/%s", URI_BASE, callerClazz.getSimpleName(), UUID.randomUUID()));
        } catch( URISyntaxException e ) {
            throw new RuntimeException(e);
        }
    }
}
