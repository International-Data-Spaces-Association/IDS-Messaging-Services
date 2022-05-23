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

import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.RequestMessage;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import ids.messaging.common.SerializeException;
import ids.messaging.protocol.multipart.parser.MultipartDatapart;
import lombok.Getter;
import org.apache.commons.io.FileUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

/**
 * Utility class for returning files using Base64 encoding.
 *
 * @param <T> a subclass of ResponseMessage or NotificationMessage
 */
@Getter
public class Base64EncodedFileBodyResponse<T extends Message> implements MessageResponse {
    /**
     * The header of the response.
     */
    private final T header;

    /**
     * The payload of the response.
     */
    private final HttpEntity<byte[]> payload;

    /**
     * Create a MessageResponse with a Payload containing a Base64 encoded File.
     *
     * @param header Header of the response (ResponseMessage or NotificationMessage).
     * @param file File that should be returned.
     * @param mediaType Mediatype of the file.
     * @throws IOException If header cannot be serialized to json, or file cannot be parsed
     * to base64 encoded string.
     */
    @SuppressWarnings(value = { "rawtypes", "unchecked" })
    public Base64EncodedFileBodyResponse(final T header, final File file, final MediaType mediaType)
            throws IOException {
        if (header instanceof RequestMessage) {
            throw new IllegalStateException("Responses are only allowed "
                + "using instances of ResponseMessage or NotificationMessage!");
        }

        this.header = header;

        final var payloadBytes = Base64.getEncoder().encode(FileUtils.readFileToByteArray(file));
        final var headers = new HttpHeaders();

        headers.setContentType(mediaType);

        this.payload = new HttpEntity(payloadBytes, headers);
    }

    /**
     * Create a MessageResponse with a Payload containing a Base64 encoded File.
     *
     * @param header Header of the response (ResponseMessage or NotificationMessage).
     * @param file File that should be returned.
     * @param mediaType Mediatype of the file.
     * @param <T> Subtype of Message (ResponseMessage or NotificationMessage).
     * @return Instance of Base64EncodedFileBodyResponse using given parameters.
     * @throws IOException If header cannot be serialized to json, or file cannot be parsed
     * to base64 encoded string.
     */
    public static <T extends Message> Base64EncodedFileBodyResponse<T> create(
            final T header, final File file, final MediaType mediaType)
            throws IOException {
        return new Base64EncodedFileBodyResponse<>(header, file, mediaType);
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
