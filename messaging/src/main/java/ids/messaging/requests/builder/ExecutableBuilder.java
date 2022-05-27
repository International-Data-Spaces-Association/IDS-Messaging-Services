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
package ids.messaging.requests.builder;

import ids.messaging.common.DeserializeException;
import ids.messaging.common.SerializeException;
import ids.messaging.core.daps.ClaimsException;
import ids.messaging.core.daps.DapsTokenManagerException;
import ids.messaging.protocol.http.SendMessageException;
import ids.messaging.protocol.http.ShaclValidatorException;
import ids.messaging.protocol.multipart.UnknownResponseException;
import ids.messaging.protocol.multipart.parser.MultipartParseException;
import ids.messaging.requests.MessageContainer;
import ids.messaging.requests.exceptions.RejectionException;
import ids.messaging.requests.exceptions.UnexpectedPayloadException;

import java.io.IOException;
import java.net.URI;

/**
 * A RequestBuilder that is able to execute a request will implement this class.
 *
 * @param <T> Type of expected payload.
 */
public interface ExecutableBuilder<T> {

    /**
     * Send the message using the current information of the builder.
     *
     * @param target targetURI message will be sent to
     * @return MessageContainer containing response
     * @throws DapsTokenManagerException when DAT cannot be received from DAPS
     * @throws ShaclValidatorException when Shacl Validation fails
     * @throws SerializeException when the payload cannot be serialized
     * @throws ClaimsException when DAT of response is not valid
     * @throws UnknownResponseException when type of response is not known
     * @throws SendMessageException when an IOException is thrown
     * by the httpclient when sending the message
     * @throws MultipartParseException when the response cannot be parsed as multipart
     * @throws IOException when some other error happens while sending the message
     * @throws DeserializeException when response cannot be deserialized
     * @throws RejectionException when response is a RejectionMessage
     * (and 'throwOnRejection' is set in the builder)
     * @throws UnexpectedPayloadException when payload is not of type T
     */
    MessageContainer<T> execute(URI target)
            throws DapsTokenManagerException,
            ShaclValidatorException,
            SerializeException,
            ClaimsException,
            UnknownResponseException,
            SendMessageException,
            MultipartParseException,
            IOException,
            DeserializeException,
            RejectionException,
            UnexpectedPayloadException;

}
