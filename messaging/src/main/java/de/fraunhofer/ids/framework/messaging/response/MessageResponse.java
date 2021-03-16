package de.fraunhofer.ids.framework.messaging.response;

import de.fraunhofer.iais.eis.ids.jsonld.Serializer;

import java.io.IOException;
import java.util.Map;

/**
 * A MessageResponse is returned by the MessageHandlers, for easy building of Responses
 * and automatic creation of Maps to be returned as Multipart Responses.
 */
public interface MessageResponse {
    /**
     * Create an empty MessageResponse
     *
     * @return an empty MessageResponse
     */
    static MessageResponse empty() {
        return ( s ) -> Map.of();
    }

    /**
     * Create a MultipartMap from the MessageResponse, with header and payload part
     *
     * @param serializer a Serializer to produce JsonLD
     *
     * @return Map of response parts that can be used for the multipart response
     *
     * @throws IOException if some object cannot be serialized
     */
    Map<String, Object> createMultipartMap( Serializer serializer ) throws IOException;
}
