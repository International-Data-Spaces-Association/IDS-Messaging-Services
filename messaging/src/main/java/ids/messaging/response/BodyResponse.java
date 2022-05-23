/*
 * Copyright Fraunhofer Institute for Software and Systems Engineering
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *  Contributors:
 *       sovity GmbH
 *
 */
package ids.messaging.response;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.RequestMessage;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import ids.messaging.common.SerializeException;
import ids.messaging.protocol.multipart.parser.MultipartDatapart;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * @param <T> a subtype of ResponseMessage or NotificationMessage (will throw
 * IllegalStateException if used with ResponseMessage).
 */
@Data
@Slf4j
@Getter
public class BodyResponse<T extends Message> implements MessageResponse {
    /**
     * The header of the response.
     */
    private final T header;

    /**
     * The payload of the response.
     */
    private final Object payload;

    /**
     * @param header ResponseMessage or NotificationMessage for the header.
     * @param payload Some Object as payload.
     */
    public BodyResponse(final T header, final Object payload) {
        if (header instanceof RequestMessage) {
            throw new IllegalStateException("Responses are only allowed "
                + "using instances of ResponseMessage or NotificationMessage!");
        }

        this.header = header;
        this.payload = payload;
    }

    /**
     * Create a MessageResponse with some Object as payload.
     *
     * @param header ResponseMessage or NotificationMessage for the header.
     * @param payload Some Object used as payload.
     * @param <T> Type of the Message (some instance of ResponseMessage or NotificationMessage).
     * @return An instance of BodyResponse with the given parameters.
     */
    public static <T extends Message> BodyResponse<T> create(final T header, final Object payload) {
        return new BodyResponse<>(header, payload);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> createMultipartMap(final Serializer serializer)
            throws SerializeException {
        try {
            final var multiMap = new LinkedHashMap<String, Object>();
            multiMap.put(MultipartDatapart.HEADER.toString(),
                         serializer.serialize(header));
            multiMap.put(MultipartDatapart.PAYLOAD.toString(), payload);

            return multiMap;
        } catch (IOException ioException) {
            throw new SerializeException(ioException);
        }
    }
}
