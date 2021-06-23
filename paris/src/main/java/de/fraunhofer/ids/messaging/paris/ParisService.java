package de.fraunhofer.ids.messaging.paris;

import java.io.IOException;
import java.net.URI;

import de.fraunhofer.iais.eis.Participant;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.ids.messaging.core.config.ConfigContainer;
import de.fraunhofer.ids.messaging.core.daps.ClaimsException;
import de.fraunhofer.ids.messaging.core.daps.DapsTokenManagerException;
import de.fraunhofer.ids.messaging.core.daps.DapsTokenProvider;
import de.fraunhofer.ids.messaging.protocol.InfrastructureService;
import de.fraunhofer.ids.messaging.protocol.MessageService;
import de.fraunhofer.ids.messaging.protocol.multipart.mapping.DescriptionResponseMAP;
import de.fraunhofer.ids.messaging.protocol.multipart.mapping.GenericMessageAndPayload;
import de.fraunhofer.ids.messaging.protocol.multipart.mapping.MessageProcessedNotificationMAP;
import de.fraunhofer.ids.messaging.protocol.multipart.mapping.ParticipantNotificationMAP;
import de.fraunhofer.ids.messaging.protocol.multipart.parser.MultipartParseException;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * ParIS Communication Controller. Generates appropriate ids multipart messages
 * and sends them to the ParIS' infrastructure api.
 */
@Slf4j
@Component
@FieldDefaults( makeFinal = true, level = AccessLevel.PRIVATE )
public class ParisService extends InfrastructureService
        implements IDSParisService {

    /**
     * @param container      the ConfigContainer
     * @param tokenProvider  the DapsTokenProvider
     * @param messageService the MessageService
     */
    public ParisService( final ConfigContainer container,
                         final DapsTokenProvider tokenProvider,
                         final MessageService messageService ) {
        super(container, tokenProvider, messageService);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MessageProcessedNotificationMAP updateParticipantAtParIS(
            final URI parisURI, final Participant participant )
            throws
            DapsTokenManagerException,
            ClaimsException,
            MultipartParseException,
            IOException {

        final var securityToken = tokenProvider.getDAT();
        final var header = MessageBuilder.buildParticipantUpdateMessage(
                securityToken,
                container.getConnector());

        final var messageAndPayload =
                new GenericMessageAndPayload(header, participant);
        final var response =
                messageService.sendIdsMessage(messageAndPayload, parisURI);
        return super.expectMessageProcessedNotificationMAP(response);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public MessageProcessedNotificationMAP unregisterAtParIS(
            final URI parisURI, final URI participantUri )
            throws
            DapsTokenManagerException,
            ClaimsException,
            MultipartParseException,
            IOException {
        final var securityToken = tokenProvider.getDAT();
        final var header = MessageBuilder.buildParticipantUnavailableMessage(
                securityToken,
                container.getConnector(), participantUri);

        final var messageAndPayload = new GenericMessageAndPayload(header);
        final var response =
                messageService.sendIdsMessage(messageAndPayload, parisURI);
        return super.expectMessageProcessedNotificationMAP(response);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ParticipantNotificationMAP requestParticipant(
            final URI parisURI, final URI participantUri )
            throws
            DapsTokenManagerException,
            ClaimsException,
            MultipartParseException,
            IOException {
        final var response =
                super.requestSelfDescription(parisURI, participantUri);
        return expectParticipant(response);

    }

    /**
     * @param response ParticipantNotificationMAP as return by
     *                 {@link MessageService}
     *
     * @return ParticipantNotificationMAP with parsed {@link Participant}
     *
     * @throws IOException if payload cannot be parsed.
     */
    private ParticipantNotificationMAP expectParticipant(
            final DescriptionResponseMAP response ) throws IOException {
        if( response.getPayload().isPresent() ) {
            final var payload = response.getPayload().get();
            final var participant =
                    new Serializer().deserialize(payload, Participant.class);
            return new ParticipantNotificationMAP(response.getMessage(),
                                                  participant);
        } else {
            throw new IOException("no Participant was returned");
        }
    }
}
