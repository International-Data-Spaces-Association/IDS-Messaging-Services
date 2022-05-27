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
package ids.messaging.paris;

import java.io.IOException;
import java.net.URI;

import de.fraunhofer.iais.eis.Participant;
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
 * Interface for service classes for ParIS communication.
 */
public interface IDSParisService {
    /**
     * Create or Update {@link Participant} at {@link de.fraunhofer.iais.eis.ParIS}.
     *
     * @param parisURI URI of the ParIS.
     * @param participant {@link Participant} to be created or updated.
     * @return MessageProcessedNotification in Message and Payload object.
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
     * @return The MessageContainer.
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
     * Receive Description of a {@link Participant} registered in the ParIS.
     *
     * @param parisURI URI of the {@link de.fraunhofer.iais.eis.ParIS}.
     * @param participantUri URI of the {@link Participant} to be requested.
     * @return The MessageContainer.
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
