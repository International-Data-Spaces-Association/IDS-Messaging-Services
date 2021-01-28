package de.fraunhofer.ids.framework.messaging.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

/**
 * Utility for generating Resource IDs for infomodel builders
 */
public final class ResourceIDGenerator {
    private static final String URIBase = "https://w3id.org/idsa/autogen";

    private ResourceIDGenerator() { }

    /**
     * Create an URI with callerClazz name and random uuid in path (used as ID URIs)
     *
     * @param callerClazz class for which the randomURI should be generated
     *
     * @return a random URI ID
     */
    public static URI randomURI( Class<?> callerClazz ) {
        try {
            return new URI(String.format("%s/%s/%s", URIBase, callerClazz.getSimpleName(), UUID.randomUUID()));
        } catch( URISyntaxException e ) {
            throw new RuntimeException(e);
        }
    }
}