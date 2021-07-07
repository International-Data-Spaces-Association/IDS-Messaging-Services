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

import de.fraunhofer.iais.eis.QueryLanguage;
import de.fraunhofer.iais.eis.QueryScope;
import de.fraunhofer.iais.eis.QueryTarget;
import de.fraunhofer.iais.eis.Resource;
import de.fraunhofer.ids.messaging.broker.util.FullTextQueryTemplate;
import de.fraunhofer.ids.messaging.common.DeserializeException;
import de.fraunhofer.ids.messaging.common.SerializeException;
import de.fraunhofer.ids.messaging.core.config.ConfigContainer;
import de.fraunhofer.ids.messaging.core.daps.ClaimsException;
import de.fraunhofer.ids.messaging.core.daps.DapsTokenManagerException;
import de.fraunhofer.ids.messaging.core.daps.DapsTokenProvider;
import de.fraunhofer.ids.messaging.protocol.MessageService;
import de.fraunhofer.ids.messaging.protocol.http.SendMessageException;
import de.fraunhofer.ids.messaging.protocol.http.ShaclValidatorException;
import de.fraunhofer.ids.messaging.protocol.multipart.UnknownResponseException;
import de.fraunhofer.ids.messaging.protocol.multipart.parser.MultipartParseException;
import de.fraunhofer.ids.messaging.requests.builder.IdsRequestBuilderService;
import de.fraunhofer.ids.messaging.requests.InfrastructureService;
import de.fraunhofer.ids.messaging.requests.MessageContainer;
import de.fraunhofer.ids.messaging.requests.NotificationTemplateProvider;
import de.fraunhofer.ids.messaging.requests.RequestTemplateProvider;
import de.fraunhofer.ids.messaging.requests.enums.Crud;
import de.fraunhofer.ids.messaging.requests.enums.ProtocolType;
import de.fraunhofer.ids.messaging.requests.enums.Subject;
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
     * @param templateProvider provider for notification message templates
     * @param requestTemplateProvider provider for request message templates
     */
    public BrokerService(final ConfigContainer container,
                         final DapsTokenProvider tokenProvider,
                         final MessageService messageService,
                         final IdsRequestBuilderService idsRequestBuilderService,
                         final NotificationTemplateProvider templateProvider,
                         final RequestTemplateProvider requestTemplateProvider) {
        super(container, tokenProvider, messageService);
        this.notificationTemplateProvider = templateProvider;
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
            ShaclValidatorException,
            SerializeException,
            UnknownResponseException,
            SendMessageException,
            DeserializeException, RejectionException, UnexpectedPayloadException {
        logBuildingHeader();
        return requestBuilderService.newRequest()
                .withPayload(resource)
                .subjectResource()
                .useMultipart()
                .operationDelete(resource.getId())
                .execute(brokerURI);
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
            ShaclValidatorException,
            SerializeException,
            UnknownResponseException,
            SendMessageException,
            DeserializeException, RejectionException, UnexpectedPayloadException {

        logBuildingHeader();
        return requestBuilderService.newRequest()
                .withPayload(resource)
                .subjectResource()
                .useMultipart()
                .operationUpdate(resource.getId())
                .execute(brokerURI);
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
            ShaclValidatorException,
            SerializeException,
            UnknownResponseException,
            SendMessageException,
            DeserializeException, RejectionException, UnexpectedPayloadException {
        logBuildingHeader();
        return requestBuilderService.newRequest()
                .withPayload(container.getConnector())
                .subjectConnector()
                .useMultipart()
                .operationDelete(container.getConnector().getId())
                .execute(brokerURI);
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
            ShaclValidatorException,
            SerializeException,
            UnknownResponseException,
            SendMessageException,
            DeserializeException, RejectionException, UnexpectedPayloadException {
        logBuildingHeader();
        return requestBuilderService.newRequest()
                .withPayload(container.getConnector())
                .subjectConnector()
                .useMultipart()
                .operationUpdate(container.getConnector().getId())
                .execute(brokerURI);
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
            ShaclValidatorException,
            SerializeException,
            UnknownResponseException,
            SendMessageException,
            DeserializeException, RejectionException, UnexpectedPayloadException {
        logBuildingHeader();
        return requestBuilderService
                .newRequest()
                .withPayload(query)
                .subjectQuery()
                .useMultipart()
                .operationSend(queryLanguage, queryScope, queryTarget)
                .execute(brokerURI);
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
            ShaclValidatorException,
            SerializeException,
            UnknownResponseException,
            SendMessageException,
            DeserializeException, RejectionException, UnexpectedPayloadException {
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
                                          final int offset)
            throws
            DapsTokenManagerException,
            IOException,
            MultipartParseException,
            ClaimsException,
            ShaclValidatorException,
            SerializeException,
            UnknownResponseException,
            SendMessageException,
            DeserializeException, RejectionException, UnexpectedPayloadException {
        final var payload = String.format(
                FullTextQueryTemplate.FULL_TEXT_QUERY,
                searchTerm, limit, offset);
        return requestBuilderService
                .newRequest()
                .withPayload(payload)
                .subjectQuery()
                .useMultipart()
                .operationSend(QueryLanguage.SPARQL, queryScope, queryTarget)
                .execute(brokerURI);
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
