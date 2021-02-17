package de.fraunhofer.ids.framework.messaging.response;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.RequestMessage;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.ids.framework.util.MultipartDatapart;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * @param <T> a subtype of ResponseMessage or NotificationMessage (will throw IllegalStateException if used with ResponseMessage)
 */
@Data
@Slf4j
public class BodyResponse<T extends Message> implements MessageResponse {

    final T      header;
    final Object payload;

    /**
     * @param header  ResponseMessage or NotificationMessage for the header
     * @param payload some Object as payload
     */
    public BodyResponse( T header, Object payload ) {
        if( header instanceof RequestMessage ) {
            throw new IllegalStateException(
                    "Responses are only allowed using instances of ResponseMessage or NotificationMessage!");
        }
        this.header = header;
        this.payload = payload;
    }

    /**
     * Create a MessageResponse with some Object as payload
     *
     * @param header  ResponseMessage or NotificationMessage for the header
     * @param payload some Object used as payload
     * @param <T>     type of the Message (some instance of ResponseMessage or NotificationMessage)
     *
     * @return an instance of BodyResponse with the given parameters
     */
    public static <T extends Message> BodyResponse<T> create( final T header, final Object payload ) {
        return new BodyResponse<>(header, payload);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> createMultipartMap( Serializer serializer ) throws IOException {
        var multiMap = new LinkedHashMap<String, Object>();
        multiMap.put(MultipartDatapart.HEADER.name(), serializer.serialize(header));
        multiMap.put(MultipartDatapart.PAYLOAD.name(), payload);
        return multiMap;
    }
}
