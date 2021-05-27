package de.fraunhofer.ids.messaging.broker;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import de.fraunhofer.iais.eis.DynamicAttributeToken;
import de.fraunhofer.iais.eis.QueryLanguage;
import de.fraunhofer.iais.eis.QueryScope;
import de.fraunhofer.iais.eis.QueryTarget;
import de.fraunhofer.iais.eis.Resource;
import de.fraunhofer.ids.messaging.core.config.ConfigContainer;
import de.fraunhofer.ids.messaging.core.daps.ClaimsException;
import de.fraunhofer.ids.messaging.core.daps.ConnectorMissingCertExtensionException;
import de.fraunhofer.ids.messaging.core.daps.DapsConnectionException;
import de.fraunhofer.ids.messaging.core.daps.DapsEmptyResponseException;
import de.fraunhofer.ids.messaging.core.daps.DapsTokenManagerException;
import de.fraunhofer.ids.messaging.core.daps.DapsTokenProvider;
import de.fraunhofer.ids.messaging.protocol.multipart.parser.MultipartParseException;
import de.fraunhofer.ids.messaging.protocol.InfrastructureService;
import de.fraunhofer.ids.messaging.protocol.MessageService;
import de.fraunhofer.ids.messaging.protocol.multipart.mapping.GenericMessageAndPayload;
import de.fraunhofer.ids.messaging.protocol.multipart.mapping.MessageProcessedNotificationMAP;
import de.fraunhofer.ids.messaging.protocol.multipart.mapping.ResultMAP;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Broker Communication Controller. Generates appropriate ids multipart messages and sends them to the broker
 * infrastructure api.
 **/

@Slf4j
@Component
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class BrokerService extends InfrastructureService
        implements IDSBrokerService {

    /**
     * BrokerService constructor.
     * @param container the ConfigContainer
     * @param tokenProvider the DapsTokenProvider
     * @param messageService the MessageService
     */
    public BrokerService(final ConfigContainer container,
                         final DapsTokenProvider tokenProvider,
                         final MessageService messageService) {
        super(container, tokenProvider, messageService);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MessageProcessedNotificationMAP removeResourceFromBroker(@NonNull final URI brokerURI,
                                                                    @NonNull final Resource resource)
            throws IOException, DapsTokenManagerException, MultipartParseException, ClaimsException {

        logBuildingHeader();

        final var securityToken = getDat();
        final var header = MessageBuilder.buildResourceUnavailableMessage(
                securityToken,
                container.getConnector(),
                resource);

        final var messageAndPayload = new GenericMessageAndPayload(header);
        final var response = messageService.sendIdsMessage(messageAndPayload, brokerURI);

        return expectMessageProcessedNotificationMAP(response);

    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public MessageProcessedNotificationMAP updateResourceAtBroker(@NonNull final URI brokerURI, @NonNull final Resource resource) throws
            IOException, DapsTokenManagerException, MultipartParseException, ClaimsException {

        logBuildingHeader();

        final var securityToken = getDat();

        final var header = MessageBuilder.buildResourceUpdateMessage(
                securityToken,
                container.getConnector(),
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
    public MessageProcessedNotificationMAP unregisterAtBroker(@NonNull final URI brokerURI)
            throws IOException, DapsTokenManagerException, MultipartParseException, ClaimsException {
        logBuildingHeader();

        final var securityToken = getDat();
        final var header = MessageBuilder.buildUnavailableMessage(securityToken, container.getConnector());
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
    public MessageProcessedNotificationMAP updateSelfDescriptionAtBroker(@NonNull final URI brokerURI) throws
            IOException, DapsTokenManagerException, MultipartParseException, ClaimsException {
        logBuildingHeader();

        final var securityToken = getDat();
        final var header = MessageBuilder.buildUpdateMessage(securityToken, container.getConnector());
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
    public List<MessageProcessedNotificationMAP> updateSelfDescriptionAtBrokers(@NonNull final List<URI> brokerURIs) {
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
    public ResultMAP queryBroker(@NonNull final URI brokerURI,
                                 @NonNull final String query,
                                 @NonNull final QueryLanguage queryLanguage,
                                 @NonNull final QueryScope queryScope,
                                 @NonNull final QueryTarget queryTarget)
            throws IOException, DapsTokenManagerException, MultipartParseException, ClaimsException {
        logBuildingHeader();

        final var securityToken = getDat();
        final var header = MessageBuilder.buildQueryMessage(
                securityToken,
                container.getConnector(),
                queryLanguage,
                queryScope,
                queryTarget);
        final var messageAndPayload = new GenericMessageAndPayload(header, null);
        final var response = messageService.sendIdsMessage(messageAndPayload, brokerURI);

        return expectResultMAP(response);
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
