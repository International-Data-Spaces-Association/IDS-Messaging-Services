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
package de.fraunhofer.ids.messaging.appstore;

import java.io.IOException;
import java.net.URI;

import de.fraunhofer.ids.messaging.common.DeserializeException;
import de.fraunhofer.ids.messaging.common.SerializeException;
import de.fraunhofer.ids.messaging.core.daps.ClaimsException;
import de.fraunhofer.ids.messaging.core.daps.DapsTokenManagerException;
import de.fraunhofer.ids.messaging.protocol.http.SendMessageException;
import de.fraunhofer.ids.messaging.protocol.http.ShaclValidatorException;
import de.fraunhofer.ids.messaging.protocol.multipart.UnknownResponseException;
import de.fraunhofer.ids.messaging.protocol.multipart.parser.MultipartParseException;
import de.fraunhofer.ids.messaging.requests.MessageContainer;
import de.fraunhofer.ids.messaging.requests.exceptions.RejectionException;
import de.fraunhofer.ids.messaging.requests.exceptions.UnexpectedPayloadException;


/**
 * Service class interface for communication to the IDS AppStore.
 */
public interface IDSAppStoreService {


    /**
     * @param appStoreURI URI of the App Store to be used.
     * @param app URI of the requested app.
     * @return Response MAP with the SelfDescription in the payload as an AppStore.
     * @throws MultipartParseException If response could not be parsed to
     * header and payload.@throws ClaimsException.
     * @throws IOException If message could not be sent
     * or Serializer could not parse RDF to Java Object.
     * @throws DapsTokenManagerException If no DAT for sending the message could be received.
     * @throws ClaimsException If DAT of incoming message could not be validated.
     */
    MessageContainer<Object> requestAppDescription(URI appStoreURI, URI app)
            throws
            ClaimsException,
            MultipartParseException,
            IOException,
            DapsTokenManagerException, ShaclValidatorException,
            SerializeException, UnknownResponseException, SendMessageException,
            DeserializeException, RejectionException,
            UnexpectedPayloadException;
    /**
     * @param appStoreURI URI of the AppStore to be used.
     * @return Response MAP with the SelfDescription in the payload as AppResource.
     * @throws MultipartParseException If response could not be parsed to
     * header and payload.@throws ClaimsException.
     * @throws IOException If message could not be sent or Serializer
     * could not parse RDF to Java Object.
     * @throws DapsTokenManagerException If no DAT for sending the message could be received.
     * @throws ClaimsException If DAT of incoming message could not be validated.
     */
    MessageContainer<Object> requestAppStoreDescription(URI appStoreURI)
            throws
            ClaimsException,
            IOException,
            DapsTokenManagerException, MultipartParseException,
            ShaclValidatorException, SerializeException,
            UnknownResponseException, SendMessageException,
            DeserializeException, RejectionException,
            UnexpectedPayloadException;
    /**
     * @param appStoreURI URI of the App Store to be used.
     * @param app  URI of the requested app.
     * @return Response MAP with the SelfDescription in the payload as String.
     * @throws MultipartParseException If response could not be parsed
     * to header and payload.@throws ClaimsException.
     * @throws IOException If message could not be sent or
     * Serializer could not parse RDF to Java Object.
     * @throws DapsTokenManagerException If no DAT for sending the message could be received.
     * @throws ClaimsException If DAT of incoming message could not be validated.
     */
    MessageContainer<Object> requestAppArtifact(URI appStoreURI, URI app)
            throws
            DapsTokenManagerException,
            ClaimsException,
            MultipartParseException,
            IOException, ShaclValidatorException, SerializeException,
            UnknownResponseException, SendMessageException,
            DeserializeException, RejectionException,
            UnexpectedPayloadException;
}
