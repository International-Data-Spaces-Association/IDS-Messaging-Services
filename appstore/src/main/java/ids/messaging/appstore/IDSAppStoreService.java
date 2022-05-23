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
package ids.messaging.appstore;

import java.io.IOException;
import java.net.URI;

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


/**
 * Service class interface for communication to the IDS AppStore.
 */
public interface IDSAppStoreService {
    /**
     * Requests description from the AppStore itself using its URI.
     *
     * @param appStoreURI URI of the App Store to be used.
     * @return Response MAP with the SelfDescription in the payload as an AppStore.
     * @throws DapsTokenManagerException If no DAT for sending the message could be received.
     * @throws ClaimsException If DAT of incoming message could not be validated.
     * @throws MultipartParseException If response could not be parsed to header and payload.
     * @throws IOException Other errors, which were not categorized.
     * @throws ShaclValidatorException If the message does not pass the SHACL validation test.
     * @throws SerializeException If there are problems with serializing.
     * @throws UnknownResponseException If the format of the answer is not known.
     * @throws SendMessageException If there is an error when sending the request.
     * @throws DeserializeException If the deserialization of the received message fails.
     * @throws RejectionException When a RejectionMessage arrives unexpectedly.
     * @throws UnexpectedPayloadException When the payload cannot be used.
     */
    MessageContainer<Object> requestAppStoreDescription(URI appStoreURI)
            throws
            ClaimsException,
            IOException,
            DapsTokenManagerException,
            MultipartParseException,
            ShaclValidatorException,
            SerializeException,
            UnknownResponseException,
            SendMessageException,
            DeserializeException,
            RejectionException,
            UnexpectedPayloadException;

    /**
     * Requests description for a specific App by its URI and the AppStore URI.
     *
     * @param appStoreURI URI of the App Store to be used.
     * @param app URI of the requested app.
     * @return Response MAP with the SelfDescription in the payload as an AppStore.
     * @throws DapsTokenManagerException If no DAT for sending the message could be received.
     * @throws ClaimsException If DAT of incoming message could not be validated.
     * @throws MultipartParseException If response could not be parsed to header and payload.
     * @throws IOException Other errors, which were not categorized.
     * @throws ShaclValidatorException If the message does not pass the SHACL validation test.
     * @throws SerializeException If there are problems with serializing.
     * @throws UnknownResponseException If the format of the answer is not known.
     * @throws SendMessageException If there is an error when sending the request.
     * @throws DeserializeException If the deserialization of the received message fails.
     * @throws RejectionException When a RejectionMessage arrives unexpectedly.
     * @throws UnexpectedPayloadException When the payload cannot be used.
     */
    MessageContainer<Object> requestAppDescription(URI appStoreURI, URI app)
            throws
            ClaimsException,
            MultipartParseException,
            IOException,
            DapsTokenManagerException,
            ShaclValidatorException,
            SerializeException,
            UnknownResponseException,
            SendMessageException,
            DeserializeException,
            RejectionException,
            UnexpectedPayloadException;

    /**
     * Requests the App artifact by its App URI and the AppStore URI.
     *
     * @param appStoreURI URI of the App Store to be used.
     * @param app URI of the requested app.
     * @return Response MAP with the SelfDescription in the payload as an AppStore.
     * @throws DapsTokenManagerException If no DAT for sending the message could be received.
     * @throws ClaimsException If DAT of incoming message could not be validated.
     * @throws MultipartParseException If response could not be parsed to header and payload.
     * @throws IOException Other errors, which were not categorized.
     * @throws ShaclValidatorException If the message does not pass the SHACL validation test.
     * @throws SerializeException If there are problems with serializing.
     * @throws UnknownResponseException If the format of the answer is not known.
     * @throws SendMessageException If there is an error when sending the request.
     * @throws DeserializeException If the deserialization of the received message fails.
     * @throws RejectionException When a RejectionMessage arrives unexpectedly.
     * @throws UnexpectedPayloadException When the payload cannot be used.
     */
    MessageContainer<Object> requestAppArtifact(URI appStoreURI, URI app)
            throws
            DapsTokenManagerException,
            ClaimsException,
            MultipartParseException,
            IOException,
            ShaclValidatorException,
            SerializeException,
            UnknownResponseException,
            SendMessageException,
            DeserializeException,
            RejectionException,
            UnexpectedPayloadException;
}
