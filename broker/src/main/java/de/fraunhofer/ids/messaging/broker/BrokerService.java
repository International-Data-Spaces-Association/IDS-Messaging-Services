package de.fraunhofer.ids.messaging.broker;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import de.fraunhofer.iais.eis.DynamicAttributeToken;
import de.fraunhofer.iais.eis.QueryLanguage;
import de.fraunhofer.iais.eis.QueryScope;
import de.fraunhofer.iais.eis.QueryTarget;
import de.fraunhofer.iais.eis.RejectionMessage;
import de.fraunhofer.iais.eis.Resource;
import de.fraunhofer.ids.messaging.core.config.ConfigContainer;
import de.fraunhofer.ids.messaging.core.daps.ClaimsException;
import de.fraunhofer.ids.messaging.core.daps.ConnectorMissingCertExtensionException;
import de.fraunhofer.ids.messaging.core.daps.DapsConnectionException;
import de.fraunhofer.ids.messaging.core.daps.DapsEmptyResponseException;
import de.fraunhofer.ids.messaging.core.daps.DapsTokenManagerException;
import de.fraunhofer.ids.messaging.core.daps.DapsTokenProvider;
import de.fraunhofer.ids.messaging.core.util.MultipartParseException;
import de.fraunhofer.ids.messaging.protocol.MessageService;
import de.fraunhofer.ids.messaging.protocol.multipart.MessageAndPayload;
import de.fraunhofer.ids.messaging.protocol.multipart.mapping.GenericMessageAndPayload;
import de.fraunhofer.ids.messaging.protocol.multipart.mapping.MessageProcessedNotificationMAP;
import de.fraunhofer.ids.messaging.protocol.multipart.mapping.RejectionMAP;
import de.fraunhofer.ids.messaging.protocol.multipart.mapping.ResultMAP;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Broker Communication Controller. Generates appropriate ids multipart messages and sends them to the broker
 * infrastructure api.
 **/
@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class BrokerService implements IDSBrokerService {
    static String INFO_MODEL_VERSION = "4.0.0";

    ConfigContainer   container;
    DapsTokenProvider tokenProvider;
    MessageService    messageService;

    /**
     * {@inheritDoc}
     */
    @Override
    public MessageProcessedNotificationMAP removeResourceFromBroker(final URI brokerURI, final Resource resource)
            throws IOException, DapsTokenManagerException, MultipartParseException, ClaimsException {

        logBuildingHeader();

        final var securityToken = getDat();
        final var connectorID = getConnectorId();
        final var header = MessageBuilder.buildResourceUnavailableMessage(
                securityToken,
                INFO_MODEL_VERSION,
                connectorID,
                resource);

        final var messageAndPayload = new GenericMessageAndPayload(header, resource);
        final var response = messageService.sendIdsMessage(messageAndPayload, brokerURI);

        return expectMessageProcessedNotificationMAP(response);

    }

    private MessageProcessedNotificationMAP expectMessageProcessedNotificationMAP(final MessageAndPayload<?, ?> response)
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
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public MessageProcessedNotificationMAP updateResourceAtBroker(final URI brokerURI, final Resource resource) throws
            IOException, DapsTokenManagerException, MultipartParseException, ClaimsException {

        logBuildingHeader();

        final var securityToken = getDat();
        final var connectorID = getConnectorId();

        final var header = MessageBuilder.buildResourceUpdateMessage(
                securityToken,
                INFO_MODEL_VERSION,
                connectorID,
                resource);

        final var messageAndPayload = new GenericMessageAndPayload(header, resource);
        final var response = messageService.sendIdsMessage(messageAndPayload, brokerURI);

        return expectMessageProcessedNotificationMAP(response);
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public MessageProcessedNotificationMAP unregisterAtBroker(final URI brokerURI)
            throws IOException, DapsTokenManagerException, MultipartParseException, ClaimsException {
        logBuildingHeader();

        final var securityToken = getDat();
        final var connectorID = getConnectorId();
        final var header = MessageBuilder.buildUnavailableMessage(securityToken, INFO_MODEL_VERSION, connectorID);
        final var payload = container.getConnector();
        final var messageAndPayload = new GenericMessageAndPayload(header, payload);
        final var response = messageService.sendIdsMessage(messageAndPayload, brokerURI);

        return expectMessageProcessedNotificationMAP(response);
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public MessageProcessedNotificationMAP updateSelfDescriptionAtBroker(final URI brokerURI) throws
            IOException, DapsTokenManagerException, MultipartParseException, ClaimsException {
        logBuildingHeader();

        final var securityToken = getDat();
        final var connectorID = getConnectorId();
        final var header = MessageBuilder.buildUpdateMessage(securityToken, INFO_MODEL_VERSION, connectorID);
        final var payload = container.getConnector();
        final var messageAndPayload = new GenericMessageAndPayload(header, payload);
        final var response = messageService.sendIdsMessage(messageAndPayload, brokerURI);

        return expectMessageProcessedNotificationMAP(response);

    }

    /**
     * {@inheritDoc}
     *
     * @param brokerURIs
     */
    @Override
    public List<MessageProcessedNotificationMAP> updateSelfDescriptionAtBrokers(final List<URI> brokerURIs) {
        final ArrayList<MessageProcessedNotificationMAP> responses = new ArrayList<>();

        for (final var uri : brokerURIs) {
            try {
                final var response = updateSelfDescriptionAtBroker(uri);

                if (log.isInfoEnabled()) {
                    log.info(String.format("Received response from %s", uri));
                }
                responses.add(response);

            } catch (IOException | MultipartParseException | ClaimsException | DapsTokenManagerException e) {
                if (log.isWarnEnabled()) {
                    log.warn(String.format("Connection to Broker %s failed!", uri));
                    log.warn(e.getMessage(), e);
                }
            }
        }
        return responses;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResultMAP queryBroker(final URI brokerURI,
                                 final String query,
                                 final QueryLanguage queryLanguage,
                                 final QueryScope queryScope,
                                 final QueryTarget queryTarget)
            throws IOException, DapsTokenManagerException, MultipartParseException, ClaimsException {
        logBuildingHeader();

        final var securityToken = getDat();
        final var connectorID = getConnectorId();
        final var header = MessageBuilder.buildQueryMessage(
                securityToken,
                INFO_MODEL_VERSION,
                connectorID,
                queryLanguage,
                queryScope,
                queryTarget);
        final var messageAndPayload = new GenericMessageAndPayload(header, null);
        final var response = messageService.sendIdsMessage(messageAndPayload, brokerURI);

        return expectResultMAP(response);
    }

    private ResultMAP expectResultMAP(final MessageAndPayload<?, ?> response) throws IOException {
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
     * Get the ID of the connector.
     *
     * @return Connector-ID URI
     */
    private URI getConnectorId() {
        return container.getConnector().getId();
    }

    /**
     * Get a new DAT from the DAPS.
     *
     * @return DAT, returned by the DAPS for the Connector
     * @throws ConnectorMissingCertExtensionException Something went wrong with the Certificate of the Connector
     * @throws DapsConnectionException                The DAPS is not reachable (wrong URL, network problems..)
     * @throws DapsEmptyResponseException             The DAPS didn't return the expected response (maybe DAPS internal Problem?)
     */
    private DynamicAttributeToken getDat()
            throws ConnectorMissingCertExtensionException, DapsConnectionException, DapsEmptyResponseException {
        return tokenProvider.getDAT();
    }

    /**
     * Log info about starting to build the header.
     */
    private void logBuildingHeader() {
        if (log.isDebugEnabled()) {
            log.debug("Building message header");
        }
    }
}