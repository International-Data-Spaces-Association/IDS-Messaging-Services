package de.fraunhofer.ids.messaging.broker;

import de.fraunhofer.iais.eis.*;
import de.fraunhofer.ids.messaging.broker.util.FullTextQueryTemplate;
import de.fraunhofer.ids.messaging.core.config.ConfigContainer;
import de.fraunhofer.ids.messaging.core.daps.*;
import de.fraunhofer.ids.messaging.protocol.MessageService;
import de.fraunhofer.ids.messaging.protocol.multipart.parser.MultipartParseException;
import de.fraunhofer.ids.messaging.requests.*;
import de.fraunhofer.ids.messaging.requests.exceptions.NoTemplateProvidedException;
import de.fraunhofer.ids.messaging.requests.exceptions.RejectionException;
import de.fraunhofer.ids.messaging.requests.exceptions.UnexpectedPayloadException;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;

/**
 * Broker Communication Controller. Generates appropriate ids multipart messages and sends them to the broker
 * infrastructure api.
 **/

@Slf4j
@Component
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class BrokerService extends InfrastructureService
        implements IDSBrokerService {

    static int DEFAULT_LIMIT = 50;
    static int DEFAULT_OFFSET = 0;

    NotificationTemplateProvider notificationTemplateProvider;
    RequestTemplateProvider requestTemplateProvider;

    /**
     * BrokerService constructor.
     * @param container the ConfigContainer
     * @param tokenProvider the DapsTokenProvider
     * @param messageService the MessageService
     * @param requestBuilderService service to send request messages
     * @param notificationTemplateProvider provider for notification message templates
     * @param requestTemplateProvider provider for request message templates
     */
    public BrokerService(final ConfigContainer container,
                         final DapsTokenProvider tokenProvider,
                         final MessageService messageService,
                         final IdsRequestBuilderService requestBuilderService,
                         final NotificationTemplateProvider notificationTemplateProvider,
                         final RequestTemplateProvider requestTemplateProvider) {
        super(container, tokenProvider, messageService, requestBuilderService);
        this.notificationTemplateProvider = notificationTemplateProvider;
        this.requestTemplateProvider = requestTemplateProvider;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MessageContainer<?> removeResourceFromBroker(@NonNull final URI brokerURI,
                                                        @NonNull final Resource resource) throws
            IOException,
            DapsTokenManagerException,
            MultipartParseException,
            ClaimsException,
            NoTemplateProvidedException {
        logBuildingHeader();
        return buildAndSend(notificationTemplateProvider.resourceUnavailableMessageTemplate(resource.getId()), resource, brokerURI);
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public MessageContainer<?> updateResourceAtBroker(@NonNull final URI brokerURI, @NonNull final Resource resource) throws
            IOException, DapsTokenManagerException, MultipartParseException, ClaimsException, NoTemplateProvidedException {

        logBuildingHeader();
        return buildAndSend(notificationTemplateProvider.resourceUpdateMessageTemplate(resource.getId()), resource, brokerURI);
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public MessageContainer<?> unregisterAtBroker(@NonNull final URI brokerURI)
            throws IOException, DapsTokenManagerException, MultipartParseException, ClaimsException, NoTemplateProvidedException {
        logBuildingHeader();
        return buildAndSend(notificationTemplateProvider.connectorUnavailableMessageTemplate((container.getConnector().getId())), container.getConnector(), brokerURI);
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public MessageContainer<?> updateSelfDescriptionAtBroker(@NonNull final URI brokerURI) throws
            IOException, DapsTokenManagerException, MultipartParseException, ClaimsException, NoTemplateProvidedException {
        logBuildingHeader();
        return buildAndSend(notificationTemplateProvider.connectorUpdateMessageTemplate((container.getConnector().getId())), container.getConnector(), brokerURI);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MessageContainer<?> queryBroker(@NonNull final URI brokerURI,
                                 @NonNull final String query,
                                 @NonNull final QueryLanguage queryLanguage,
                                 @NonNull final QueryScope queryScope,
                                 @NonNull final QueryTarget queryTarget)
            throws IOException, DapsTokenManagerException, MultipartParseException, ClaimsException, NoTemplateProvidedException {
        logBuildingHeader();
        return buildAndSend(requestTemplateProvider.queryMessageTemplate(queryLanguage, queryScope, queryTarget), query, brokerURI);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MessageContainer<?> fullTextSearchBroker(final URI brokerURI,
                                          final String searchTerm,
                                          final QueryScope queryScope,
                                          final QueryTarget queryTarget)
            throws
            DapsTokenManagerException,
            IOException,
            MultipartParseException,
            ClaimsException, NoTemplateProvidedException {
        return fullTextSearchBroker(brokerURI,
                                    searchTerm,
                                    queryScope,
                                    queryTarget,
                                    DEFAULT_LIMIT,
                                    DEFAULT_OFFSET);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MessageContainer<?> fullTextSearchBroker(final URI brokerURI,
                                          final String searchTerm,
                                          final QueryScope queryScope,
                                          final QueryTarget queryTarget,
                                          final int limit,
                                          final int offset )
            throws
            DapsTokenManagerException,
            IOException,
            MultipartParseException,
            ClaimsException, NoTemplateProvidedException {
        final var payload = String.format(
                FullTextQueryTemplate.FULL_TEXT_QUERY,
                searchTerm, limit, offset);
        return buildAndSend(requestTemplateProvider.queryMessageTemplate(QueryLanguage.SPARQL, queryScope, queryTarget), payload, brokerURI);
    }

    /**
     * Build IDS Message and send to a broker.
     *
     * @param template message template used for header
     * @param payload payload of message
     * @param brokerURI URI of broker message should be sent to
     * @return response from the broker packed inside a {@link MessageContainer}
     * @throws DapsTokenManagerException when DAT for message cannot be received
     * @throws ClaimsException when DAT of response cannot be parsed
     * @throws MultipartParseException when Response cannot be parsed to multipart
     * @throws NoTemplateProvidedException when template is null
     */
    private MessageContainer<?> buildAndSend(MessageTemplate<?> template, Object payload, URI brokerURI) throws DapsTokenManagerException, ClaimsException, MultipartParseException, NoTemplateProvidedException, IOException {
        try {
            return requestBuilderService
                    .newRequest()
                    .useTemplate(template)
                    .withPayload(payload)
                    .execute(brokerURI);
        } catch (RejectionException | UnexpectedPayloadException e) {
            throw new IllegalStateException(String.format("%s should never be thrown here.", e.getClass().getSimpleName()));
        }
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
