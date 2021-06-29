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

import de.fraunhofer.iais.eis.*;
import de.fraunhofer.ids.messaging.broker.util.FullTextQueryTemplate;
import de.fraunhofer.ids.messaging.common.DeserializeException;
import de.fraunhofer.ids.messaging.common.SerializeException;
import de.fraunhofer.ids.messaging.core.config.ConfigContainer;
import de.fraunhofer.ids.messaging.core.daps.*;
import de.fraunhofer.ids.messaging.protocol.MessageService;
import de.fraunhofer.ids.messaging.protocol.http.SendMessageException;
import de.fraunhofer.ids.messaging.protocol.http.ShaclValidatorException;
import de.fraunhofer.ids.messaging.protocol.multipart.UnknownResponseException;
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
    IdsRequestBuilderService requestBuilderService;

    /**
     * BrokerService constructor.
     * @param container the ConfigContainer
     * @param tokenProvider the DapsTokenProvider
     * @param messageService the MessageService
     * @param idsRequestBuilderService service to send request messages
     * @param notificationTemplateProvider provider for notification message templates
     * @param requestTemplateProvider provider for request message templates
     */
    public BrokerService(final ConfigContainer container,
                         final DapsTokenProvider tokenProvider,
                         final MessageService messageService,
                         final IdsRequestBuilderService idsRequestBuilderService,
                         final NotificationTemplateProvider notificationTemplateProvider,
                         final RequestTemplateProvider requestTemplateProvider) {
        super(container, tokenProvider, messageService);
        this.notificationTemplateProvider = notificationTemplateProvider;
        this.requestTemplateProvider = requestTemplateProvider;
        this.requestBuilderService = idsRequestBuilderService;
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
            NoTemplateProvidedException,
            ShaclValidatorException,
            SerializeException,
            UnknownResponseException,
            SendMessageException,
            DeserializeException {
        logBuildingHeader();
        return buildAndSend(notificationTemplateProvider.resourceUnavailableMessageTemplate(resource.getId()), resource, brokerURI);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MessageContainer<?> updateResourceAtBroker(@NonNull final URI brokerURI, @NonNull final Resource resource) throws
            IOException,
            DapsTokenManagerException,
            MultipartParseException,
            ClaimsException,
            NoTemplateProvidedException,
            ShaclValidatorException,
            SerializeException,
            UnknownResponseException,
            SendMessageException,
            DeserializeException {

        logBuildingHeader();
        return buildAndSend(notificationTemplateProvider.resourceUpdateMessageTemplate(resource.getId()), resource, brokerURI);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MessageContainer<?> unregisterAtBroker(@NonNull final URI brokerURI)
            throws IOException,
            DapsTokenManagerException,
            MultipartParseException,
            ClaimsException,
            NoTemplateProvidedException,
            ShaclValidatorException,
            SerializeException,
            UnknownResponseException,
            SendMessageException,
            DeserializeException {
        logBuildingHeader();
        return buildAndSend(notificationTemplateProvider.connectorUnavailableMessageTemplate((container.getConnector().getId())), container.getConnector(), brokerURI);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MessageContainer<?> updateSelfDescriptionAtBroker(@NonNull final URI brokerURI) throws
            IOException,
            DapsTokenManagerException,
            MultipartParseException,
            ClaimsException,
            NoTemplateProvidedException,
            ShaclValidatorException,
            SerializeException,
            UnknownResponseException,
            SendMessageException,
            DeserializeException {
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
            throws IOException,
            DapsTokenManagerException,
            MultipartParseException,
            ClaimsException,
            NoTemplateProvidedException,
            ShaclValidatorException,
            SerializeException,
            UnknownResponseException,
            SendMessageException,
            DeserializeException {
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
            throws DapsTokenManagerException,
            IOException,
            MultipartParseException,
            ClaimsException,
            NoTemplateProvidedException,
            ShaclValidatorException,
            SerializeException,
            UnknownResponseException,
            SendMessageException,
            DeserializeException {
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
            ClaimsException,
            NoTemplateProvidedException,
            ShaclValidatorException,
            SerializeException,
            UnknownResponseException,
            SendMessageException,
            DeserializeException {
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
    private MessageContainer<?> buildAndSend(MessageTemplate<?> template, Object payload, URI brokerURI)
            throws DapsTokenManagerException,
            ClaimsException,
            MultipartParseException,
            NoTemplateProvidedException,
            IOException,
            ShaclValidatorException,
            SerializeException,
            UnknownResponseException,
            SendMessageException,
            DeserializeException {
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
     * Log info about starting to build the header.
     */
    private void logBuildingHeader() {
        if (log.isDebugEnabled()) {
            log.debug("Building message header");
        }
    }
}
