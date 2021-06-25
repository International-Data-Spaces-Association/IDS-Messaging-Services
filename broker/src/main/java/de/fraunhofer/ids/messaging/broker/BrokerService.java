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
import de.fraunhofer.ids.messaging.broker.util.FullTextQueryTemplate;
import de.fraunhofer.ids.messaging.core.config.ConfigContainer;
import de.fraunhofer.ids.messaging.core.daps.ClaimsException;
import de.fraunhofer.ids.messaging.core.daps.ConnectorMissingCertExtensionException;
import de.fraunhofer.ids.messaging.core.daps.DapsConnectionException;
import de.fraunhofer.ids.messaging.core.daps.DapsEmptyResponseException;
import de.fraunhofer.ids.messaging.core.daps.DapsTokenManagerException;
import de.fraunhofer.ids.messaging.core.daps.DapsTokenProvider;
import de.fraunhofer.ids.messaging.protocol.InfrastructureService;
import de.fraunhofer.ids.messaging.protocol.MessageService;
import de.fraunhofer.ids.messaging.protocol.SerializeException;
import de.fraunhofer.ids.messaging.protocol.UnexpectedResponseException;
import de.fraunhofer.ids.messaging.protocol.DeserializeException;
import de.fraunhofer.ids.messaging.protocol.http.SendMessageException;
import de.fraunhofer.ids.messaging.protocol.http.ShaclValidatorException;
import de.fraunhofer.ids.messaging.protocol.multipart.UnknownResponseException;
import de.fraunhofer.ids.messaging.protocol.multipart.mapping.GenericMessageAndPayload;
import de.fraunhofer.ids.messaging.protocol.multipart.mapping.MessageProcessedNotificationMAP;
import de.fraunhofer.ids.messaging.protocol.multipart.mapping.ResultMAP;
import de.fraunhofer.ids.messaging.protocol.multipart.parser.MultipartParseException;
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
public class BrokerService extends InfrastructureService implements IDSBrokerService {

    static int DEFAULT_LIMIT = 50;
    static int DEFAULT_OFFSET = 0;
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
            throws
            IOException,
            DapsTokenManagerException,
            MultipartParseException,
            ClaimsException,
            UnknownResponseException,
            DeserializeException,
            UnexpectedResponseException,
            SerializeException,
            ShaclValidatorException,
            SendMessageException {

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
    public MessageProcessedNotificationMAP updateResourceAtBroker(@NonNull final URI brokerURI, @NonNull final Resource resource)
            throws
            IOException,
            DapsTokenManagerException,
            MultipartParseException,
            ClaimsException,
            UnknownResponseException,
            DeserializeException,
            UnexpectedResponseException,
            SerializeException,
            ShaclValidatorException,
            SendMessageException {

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
            throws
            IOException,
            DapsTokenManagerException,
            MultipartParseException,
            ClaimsException,
            UnknownResponseException,
            DeserializeException,
            UnexpectedResponseException,
            SerializeException,
            ShaclValidatorException,
            SendMessageException {
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
    public MessageProcessedNotificationMAP updateSelfDescriptionAtBroker(@NonNull final URI brokerURI)
            throws
            IOException,
            DapsTokenManagerException,
            MultipartParseException,
            ClaimsException,
            UnknownResponseException,
            DeserializeException,
            UnexpectedResponseException,
            SerializeException,
            ShaclValidatorException,
            SendMessageException {
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
    public List<MessageProcessedNotificationMAP> updateSelfDescriptionAtBrokers(@NonNull final List<URI> brokerURIs)
            throws
            IOException,
            DeserializeException,
            ShaclValidatorException,
            UnexpectedResponseException,
            SerializeException,
            DapsTokenManagerException,
            MultipartParseException,
            ClaimsException,
            SendMessageException,
            UnknownResponseException {
        final ArrayList<MessageProcessedNotificationMAP> responses = new ArrayList<>();

        for (final var uri : brokerURIs) {
            final var response = updateSelfDescriptionAtBroker(uri);

            if (log.isInfoEnabled()) {
                log.info(String.format("Received response from %s", uri));
            }
            responses.add(response);
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
            throws
            IOException,
            DapsTokenManagerException,
            MultipartParseException,
            ClaimsException,
            UnknownResponseException,
            DeserializeException,
            UnexpectedResponseException,
            SerializeException,
            ShaclValidatorException,
            SendMessageException {
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
     * {@inheritDoc}
     */
    @Override
    public ResultMAP fullTextSearchBroker(final URI brokerURI,
                                          final String searchTerm,
                                          final QueryScope queryScope,
                                          final QueryTarget queryTarget)
            throws
            ConnectorMissingCertExtensionException,
            DapsConnectionException,
            DapsEmptyResponseException,
            IOException,
            MultipartParseException,
            ClaimsException,
            UnknownResponseException,
            DeserializeException,
            UnexpectedResponseException,
            SerializeException,
            ShaclValidatorException,
            SendMessageException {
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
    public ResultMAP fullTextSearchBroker(final URI brokerURI,
                                          final String searchTerm,
                                          final QueryScope queryScope,
                                          final QueryTarget queryTarget,
                                          final int limit,
                                          final int offset )
            throws
            ConnectorMissingCertExtensionException,
            DapsConnectionException,
            DapsEmptyResponseException,
            IOException,
            MultipartParseException,
            ClaimsException,
            UnknownResponseException,
            DeserializeException,
            UnexpectedResponseException,
            SerializeException,
            ShaclValidatorException,
            SendMessageException {
        var securityToken = getDat();
        var header = MessageBuilder
                .buildQueryMessage(securityToken,
                                   container.getConnector(),
                                   QueryLanguage.SPARQL,
                                   queryScope,
                                   queryTarget);

        final var payload = String.format(
                FullTextQueryTemplate.FULL_TEXT_QUERY,
                searchTerm, limit, offset);
        final var messageAndPayload = new GenericMessageAndPayload(header, payload);
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
