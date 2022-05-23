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
package ids.messaging.protocol.multipart;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import ids.messaging.common.SerializeException;
import ids.messaging.protocol.multipart.parser.MultipartDatapart;
import ids.messaging.protocol.RequestBuilder;
import lombok.NoArgsConstructor;
import okhttp3.MultipartBody;
import okhttp3.Request;

/**
 * Class for building multipart requests.
 */
@NoArgsConstructor
public class MultipartRequestBuilder implements RequestBuilder {

    /**
     * The infomodel serializer.
     */
    private static final Serializer SERIALIZER = new Serializer();

    /**
     * {@inheritDoc}
     */
    @Override
    public Request build(final Message message, final URI target)
            throws MalformedURLException, SerializeException {
        try {
            final var body = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart(MultipartDatapart.HEADER.toString(),
                                     SERIALIZER.serialize(message))
                    .build();

        return new Request.Builder()
                .url(target.toURL())
                .post(body)
                .build();
        } catch (MalformedURLException malformedURLException) {
            //taget.toUrl threw malformedURLException
            throw malformedURLException;
        } catch (IOException ioException) {
            //SERIALIZER.serialize(message) threw IOException
            throw new SerializeException(ioException);
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Request build(final Message message, final URI target, final String payload)
            throws SerializeException, MalformedURLException {
        try {
            final var body = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart(MultipartDatapart.HEADER.toString(),
                                     SERIALIZER.serialize(message))
                    .addFormDataPart(MultipartDatapart.PAYLOAD.toString(),
                                     payload)
                    .build();

            return new Request.Builder()
                    .url(target.toURL())
                    .post(body)
                    .build();
        } catch (MalformedURLException malformedURLException) {
            //taget.toUrl threw malformedURLException
            throw malformedURLException;
        } catch (IOException ioException) {
            //SERIALIZER.serialize(message) threw IOException
            throw new SerializeException(ioException);
        }
    }
}
