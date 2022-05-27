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
package ids.messaging.util;

import java.io.File;
import java.io.IOException;

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import ids.messaging.common.SerializeException;
import ids.messaging.protocol.multipart.parser.MultipartDatapart;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

/**
 * This Builder is a utility class for building OkHTTP.
 * Multipart RequestBodies with RequestMessage header and String or File payload Part.
 */
public final class InfomodelMessageBuilder {

    /**
     * The infomodel serzializer.
     */
    private static final Serializer SERIALIZER = new Serializer();

    /**
     * The MultipartBody builder.
     */
    private final MultipartBody.Builder builder;

    /**
     * Internal builder used by the static methods.
     *
     * @param header The header Part of the MultipartMessage (an implementation of {@link Message}).
     * @throws SerializeException If the given header cannot be serialized by the given serializer.
     */
    private InfomodelMessageBuilder(final Message header) throws SerializeException {
        try {
            this.builder = new MultipartBody.Builder();
            builder.setType(MultipartBody.FORM);
            builder.addFormDataPart(MultipartDatapart.HEADER.toString(),
                                    SERIALIZER.serialize(header));
        } catch (IOException ioException) {
            throw new SerializeException(ioException);
        }
    }

    /**
     * Build a MultipartMessage with {@link Message} header and {@link File} payload.
     *
     * @param header The header Part of the MultipartMessage (an implementation of {@link Message}).
     * @param payload The File that is added to the MultipartMessages payload.
     * @param fileType The MediaType of the file.
     * @return The built Message as OkHttp MultipartBody.
     * @throws SerializeException If the given header cannot be serialized by the given serializer.
     */
    public static MultipartBody messageWithFile(final Message header,
                                                final File payload,
                                                final MediaType fileType)
            throws SerializeException {
        final var imb = new InfomodelMessageBuilder(header);
        imb.addPayload(payload, fileType);
        return imb.getRequestBody();
    }

    /**
     * Build a MultipartMessage with {@link Message} header and String payload.
     *
     * @param header The header Part of the MultipartMessage (an implementation of {@link Message}).
     * @param payload The (String) payload that is added to the MultipartMessages Payload.
     * @return The built Message as OkHttp MultipartBody.
     * @throws SerializeException If the given header cannot be serialized by the given serializer.
     */
    public static MultipartBody messageWithString(final Message header, final String payload)
            throws SerializeException {
        final var imb = new InfomodelMessageBuilder(header);
        imb.addPayload(payload);
        return imb.getRequestBody();
    }

    /**
     * Add a String payload to the builder.
     *
     * @param payload The (String) payload that is added to the MultipartMessages Payload.
     */
    private void addPayload(final String payload) {
        builder.addFormDataPart(MultipartDatapart.PAYLOAD.toString(), payload);
    }

    /**
     * Add a File payload to the builder.
     *
     * @param file The File that is added to the MultipartMessages payload.
     * @param fileType The MediaType of the file.
     */
    private void addPayload(final File file, final MediaType fileType) {
        builder.addFormDataPart(MultipartDatapart.PAYLOAD.toString(), file.getName(),
                                RequestBody.create(file, fileType));
    }

    /**
     * Getter for the built multipart message as OkHttp {@link MultipartBody}.
     *
     * @return The built Message as OkHttp {@link MultipartBody}.
     */
    private MultipartBody getRequestBody() {
        return builder.build();
    }
}
