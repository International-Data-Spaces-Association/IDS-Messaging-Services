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

public interface IDSParisService {
    /**
     * Create or Update {@link Participant}
     * at {@link de.fraunhofer.iais.eis.ParIS}.
     * @param parisURI URI of the ParIS
     * @param participant {@link Participant} to be created or updated
     *
     * @return MessageProcessedNotification in Message and Payload object
     *
     * @throws ClaimsException if DAT of incoming message could not be validated.
     * @throws MultipartParseException if response could not be parsed to header
     * and payload.
     * @throws IOException if message could not be sent or Serializer could not
     * parse RDF to Java Object.
     * @throws DapsTokenManagerException if no DAT for sending the message could
     * be received.
     */
    MessageContainer<?> updateParticipantAtParIS(
            URI parisURI,
            Participant participant )
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
     * @param parisURI URI of the ParIS
     * @param participantURI URI of the {@ link Participant} to be unregistered
     *
     * @return MessageProcessedNotification in Message and Payload object
     *
     * @throws DapsTokenManagerException if no DAT for sending the message could
     * be received.
     * @throws ClaimsException if DAT of incoming message could not be validated.
     * @throws MultipartParseException if response could not be parsed to header
     * and payload.
     * @throws IOException if message could not be sent or Serializer could not
     * parse RDF to Java Object.
     * @return
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
     * @param parisURI URI of the {@link de.fraunhofer.iais.eis.ParIS}
     * @param participantUri URI of the {@link Participant} to be requested
     *
     * @return Description Response Message and requested Participant
     *
     * @throws DapsTokenManagerException if no DAT for sending the message could
     * be received.
     * @throws ClaimsException if DAT of incoming message could not be validated.
     * @throws MultipartParseException  if response could not be parsed to header
     * and payload.
     * @throws IOException if message could not be sent or Serializer could not
     * parse RDF to Java Object.
     */
    MessageContainer<Object> requestParticipant(
            URI parisURI, URI participantUri)
            throws
            DapsTokenManagerException,
            ClaimsException,
            MultipartParseException,
            IOException, ShaclValidatorException, SerializeException,
            RejectionException, UnknownResponseException, SendMessageException,
            UnexpectedPayloadException, DeserializeException;




}
