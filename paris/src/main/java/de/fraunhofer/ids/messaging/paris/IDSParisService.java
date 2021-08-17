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
package de.fraunhofer.ids.messaging.paris;

import java.io.IOException;
import java.net.URI;

import de.fraunhofer.iais.eis.Participant;
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
 * Interface for service classes for ParIS communication.
 */
public interface IDSParisService {
    /**
     * Create or Update {@link Participant} at {@link de.fraunhofer.iais.eis.ParIS}.
     *
     * @param parisURI URI of the ParIS.
     * @param participant {@link Participant} to be created or updated.
     * @return MessageProcessedNotification in Message and Payload object.
     * @throws ClaimsException If DAT of incoming message could not be validated.
     * @throws MultipartParseException If response could not be parsed to header and payload.
     * @throws IOException If message could not be sent or Serializer could not
     * parse RDF to Java Object.
     * @throws DapsTokenManagerException If no DAT for sending the message could be received.
     */
    MessageContainer<?> updateParticipantAtParIS(
            URI parisURI,
            Participant participant)
            throws
            DapsTokenManagerException,
            ClaimsException,
            MultipartParseException,
            IOException,
            ShaclValidatorException,
            SerializeException,
            RejectionException,
            UnknownResponseException,
            SendMessageException,
            UnexpectedPayloadException,
            DeserializeException;

    /**
     * Unregister the connector at the ParIS.
     *
     * @param parisURI URI of the ParIS.
     * @param participantURI URI of the {@link Participant} to be unregistered.
     * @throws DapsTokenManagerException If no DAT for sending the message could be received.
     * @throws ClaimsException If DAT of incoming message could not be validated.
     * @throws MultipartParseException If response could not be parsed to header and payload.
     * @throws IOException If message could not be sent or Serializer could not parse RDF
     * to Java Object.
     * @return The MessageContainer.
     */
    MessageContainer<?> unregisterAtParIS(
            URI parisURI, URI participantURI)
            throws
            DapsTokenManagerException,
            ClaimsException,
            MultipartParseException,
            IOException,
            ShaclValidatorException,
            SerializeException,
            RejectionException,
            UnknownResponseException,
            SendMessageException,
            UnexpectedPayloadException,
            DeserializeException;

    /**
     *
     * Receive Description of a {@link Participant} registered in the ParIS.
     *
     * @param parisURI URI of the {@link de.fraunhofer.iais.eis.ParIS}.
     * @param participantUri URI of the {@link Participant} to be requested.
     * @throws DapsTokenManagerException If no DAT for sending the message could be received.
     * @throws ClaimsException If DAT of incoming message could not be validated.
     * @throws MultipartParseException If response could not be parsed to header and payload.
     * @throws IOException If message could not be sent or Serializer could not parse RDF
     * to Java Object.
     * @return The MessageContainer.
     */
    MessageContainer<Object> requestParticipant(
            URI parisURI, URI participantUri)
            throws
            DapsTokenManagerException,
            ClaimsException,
            MultipartParseException,
            IOException,
            ShaclValidatorException,
            SerializeException,
            RejectionException,
            UnknownResponseException,
            SendMessageException,
            UnexpectedPayloadException,
            DeserializeException;
}
