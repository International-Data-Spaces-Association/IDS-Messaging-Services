package de.fraunhofer.ids.messaging.protocol;

import java.io.IOException;
import java.net.URI;

import de.fraunhofer.iais.eis.DescriptionRequestMessageBuilder;
import de.fraunhofer.iais.eis.DynamicAttributeToken;
import de.fraunhofer.iais.eis.RejectionMessage;
import de.fraunhofer.ids.messaging.core.config.ConfigContainer;
import de.fraunhofer.ids.messaging.core.daps.*;
import de.fraunhofer.ids.messaging.protocol.multipart.MessageAndPayload;
import de.fraunhofer.ids.messaging.protocol.multipart.mapping.*;
import de.fraunhofer.ids.messaging.protocol.multipart.parser.MultipartParseException;
import de.fraunhofer.ids.messaging.util.IdsMessageUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PROTECTED)
public class InfrastructureService  {
    ConfigContainer   container;
    DapsTokenProvider tokenProvider;
    MessageService    messageService;

    /**
     * {@inheritDoc}
     */
    public DescriptionResponseMAP requestSelfDescription(final URI uri) throws
            IOException, DapsTokenManagerException, MultipartParseException, ClaimsException {
        final var header =   new DescriptionRequestMessageBuilder()
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._modelVersion_(container.getConnector().getOutboundModelVersion())
                ._issuerConnector_(container.getConnector().getId())
                ._senderAgent_(container.getConnector().getId())
                ._securityToken_(tokenProvider.getDAT())
                .build();
        final var messageAndPayload = new GenericMessageAndPayload(header);
        final var response = messageService.sendIdsMessage(messageAndPayload, uri);

        return expectDescriptionResponseMAP(response);

    }

    /**
     * casts generic {@link MessageAndPayload} to {@link DescriptionResponseMAP}.
     *
     * @param response {@link MessageAndPayload} as returned by the {@link MessageService}
     * @return {@link MessageAndPayload object specialized to the expected Message}
     * @throws IOException if a rejection message or any other unexpected message was returned.
     */
    private DescriptionResponseMAP expectDescriptionResponseMAP(final MessageAndPayload<?, ?> response)
            throws IOException {
        if (response instanceof DescriptionResponseMAP) {
            return (DescriptionResponseMAP) response;
        }

        if (response instanceof RejectionMAP) {
            final var rejectionMessage = (RejectionMessage) response.getMessage();
            throw new IOException("Message rejected by target with following Reason: " + rejectionMessage.getRejectionReason());
        }

        throw new IOException(String.format("Unexpected Message of type %s was returned", response.getMessage().getClass().toString()));
    }

    /**
     * Casts generic {@link MessageAndPayload} to {@link MessageProcessedNotificationMAP}.
     *
     * @param response {@link MessageAndPayload} as returned by the {@link MessageService}
     * @return {@link MessageAndPayload object specialized to the expected Message}
     * @throws IOException if a rejection message or any other unexpected message was returned.
     */
    protected MessageProcessedNotificationMAP expectMessageProcessedNotificationMAP(final MessageAndPayload<?, ?> response)
            throws IOException {

        if (response instanceof MessageProcessedNotificationMAP) {
            return (MessageProcessedNotificationMAP) response;
        }

        if (response instanceof RejectionMAP) {
            final var rejectionMessage = (RejectionMessage) response.getMessage();
            throw new IOException("Message rejected by target with following Reason: " + rejectionMessage.getRejectionReason());
        }

        throw new IOException(String.format("Unexpected Message of type %s was returned", response.getMessage().getClass().toString()));
    }

    /**
     * Casts generic {@link MessageAndPayload} to {@link ResultMAP}.
     *
     * @param response {@link MessageAndPayload} as returned by the {@link MessageService}
     * @return {@link MessageAndPayload object specialized to the expected Message}
     * @throws IOException if a rejection message or any other unexpected message was returned.
     */
    protected ResultMAP expectResultMAP(final MessageAndPayload<?, ?> response) throws IOException {
        if (response instanceof ResultMAP) {
            return (ResultMAP) response;
        }

        if (response instanceof RejectionMAP) {
            final var rejectionMessage = (RejectionMessage) response.getMessage();
            throw new IOException("Message rejected by target with following Reason: " + rejectionMessage.getRejectionReason());
        }

        throw new IOException(String.format("Unexpected Message of type %s was returned", response.getMessage().getClass().toString()));
    }
    /**
     * Get a new DAT from the DAPS.
     *
     * @return DAT, returned by the DAPS for the Connector
     * @throws ConnectorMissingCertExtensionException Something went wrong with the Certificate of the Connector
     * @throws DapsConnectionException                The DAPS is not reachable (wrong URL, network problems..)
     * @throws DapsEmptyResponseException             The DAPS didn't return the expected response (maybe DAPS internal Problem?)
     */
    protected DynamicAttributeToken getDat()
            throws ConnectorMissingCertExtensionException, DapsConnectionException, DapsEmptyResponseException {
        return tokenProvider.getDAT();
    }

    /**
     * Log info about starting to build the header.
     */
    protected void logBuildingHeader() {
        if (log.isDebugEnabled()) {
            log.debug("Building message header");
        }
    }

}
