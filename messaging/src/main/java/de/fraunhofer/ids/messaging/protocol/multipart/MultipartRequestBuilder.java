/*
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
 */
package de.fraunhofer.ids.messaging.protocol.multipart;

import java.io.IOException;
import java.net.URI;

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.ids.messaging.protocol.multipart.parser.MultipartDatapart;
import de.fraunhofer.ids.messaging.protocol.RequestBuilder;
import okhttp3.MultipartBody;
import okhttp3.Request;

public class MultipartRequestBuilder implements RequestBuilder {
    private static final Serializer SERIALIZER = new Serializer();

    /**
     * {@inheritDoc}
     */
    @Override
    public Request build(final Message message, final URI target) throws IOException {
        final var body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(MultipartDatapart.HEADER.toString(), SERIALIZER.serialize(message))
                .build();

        return new Request.Builder()
                .url(target.toURL())
                .post(body)
                .build();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Request build(final Message message, final URI target, final String payload) throws IOException {
        final var body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(MultipartDatapart.HEADER.toString(), SERIALIZER.serialize(message))
                .addFormDataPart(MultipartDatapart.PAYLOAD.toString(), payload)
                .build();

        return new Request.Builder()
                .url(target.toURL())
                .post(body)
                .build();
    }
}
