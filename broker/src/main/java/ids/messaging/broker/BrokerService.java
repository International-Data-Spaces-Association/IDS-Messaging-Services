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
package ids.messaging.broker;

import java.io.IOException;
import java.net.URI;

import de.fraunhofer.iais.eis.QueryLanguage;
import de.fraunhofer.iais.eis.QueryScope;
import de.fraunhofer.iais.eis.QueryTarget;
import de.fraunhofer.iais.eis.Resource;
import ids.messaging.common.DeserializeException;
import ids.messaging.common.SerializeException;
import ids.messaging.core.config.ConfigContainer;
import ids.messaging.core.daps.ClaimsException;
import ids.messaging.core.daps.DapsTokenManagerException;
import ids.messaging.core.daps.DapsTokenProvider;
import ids.messaging.protocol.MessageService;
import ids.messaging.protocol.http.SendMessageException;
import ids.messaging.protocol.http.ShaclValidatorException;
import ids.messaging.protocol.multipart.UnknownResponseException;
import ids.messaging.protocol.multipart.parser.MultipartParseException;
import ids.messaging.requests.InfrastructureService;
import ids.messaging.requests.MessageContainer;
import ids.messaging.requests.NotificationTemplateProvider;
import ids.messaging.requests.QueryService;
import ids.messaging.requests.RequestTemplateProvider;
import ids.messaging.requests.builder.IdsRequestBuilderService;
import ids.messaging.requests.exceptions.RejectionException;
import ids.messaging.requests.exceptions.UnexpectedPayloadException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Broker Communication Controller. Generates appropriate ids multipart
 * messages and sends them to the broker infrastructure api.
 */

@Slf4j
@Component
public class BrokerService extends InfrastructureService implements IDSBrokerService {

    /**
     * Default item limit for the query.
     */
    private static final int DEFAULT_LIMIT = 50;

    /**
     * Default item offset for the query.
     */
    private static final int DEFAULT_OFFSET = 0;

    /**
     * The NotificationTemplateProvider.
     */
    private final NotificationTemplateProvider notificationTemplateProvider;

    /**
     * The RequestTemplateProvider.
     */
    private final RequestTemplateProvider requestTemplateProvider;

    /**
     * The IdsRequestBuilderService.
     */
    private final IdsRequestBuilderService idsRequestBuilderService;

    /**
     * The QueryService.
     */
    private final QueryService queryService;

    /**
     * BrokerService constructor.
     *
     * @param container The ConfigContainer.
     * @param tokenProvider The DapsTokenProvider.
     * @param messageService The MessageService.
     * @param idsRequestBuilderService Service to send request messages.
     * @param templateProvider Provider for notification message templates.
     * @param requestTemplateProvider Provider for request message templates.
     */
    public BrokerService(final ConfigContainer container,
                         final DapsTokenProvider tokenProvider,
                         final MessageService messageService,
                         final IdsRequestBuilderService idsRequestBuilderService,
                         final NotificationTemplateProvider templateProvider,
                         final RequestTemplateProvider requestTemplateProvider) {
        super(container, tokenProvider, messageService, idsRequestBuilderService);
        this.notificationTemplateProvider = templateProvider;
        this.requestTemplateProvider = requestTemplateProvider;
        this.idsRequestBuilderService = idsRequestBuilderService;

        queryService = new QueryService(
                container,
                tokenProvider,
                messageService,
                idsRequestBuilderService);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MessageContainer<?> removeResourceFromBroker(
            @NonNull final URI brokerURI,
            @NonNull final Resource resource) throws
            IOException,
            DapsTokenManagerException,
            MultipartParseException,
            ClaimsException,
            ShaclValidatorException,
            SerializeException,
            UnknownResponseException,
            SendMessageException,
            DeserializeException,
            RejectionException,
            UnexpectedPayloadException {
        logBuildingHeader();
        return idsRequestBuilderService.newRequest()
                                       .subjectResource()
                                       .useMultipart()
                                       .operationDelete(resource.getId())
                                       .execute(brokerURI);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MessageContainer<?> updateResourceAtBroker(
            @NonNull final URI brokerURI, @NonNull final Resource resource)
            throws
            IOException,
            DapsTokenManagerException,
            MultipartParseException,
            ClaimsException,
            ShaclValidatorException,
            SerializeException,
            UnknownResponseException,
            SendMessageException,
            DeserializeException,
            RejectionException,
            UnexpectedPayloadException {

        logBuildingHeader();
        return idsRequestBuilderService.newRequest()
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
            DeserializeException,
            RejectionException,
            UnexpectedPayloadException {
        logBuildingHeader();
        return idsRequestBuilderService.newRequest()
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
            DeserializeException,
            RejectionException,
            UnexpectedPayloadException {
        logBuildingHeader();
        return idsRequestBuilderService.newRequest()
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
    public MessageContainer<String> queryBroker(@NonNull final URI brokerURI,
                                 @NonNull final String query,
                                 @NonNull final QueryLanguage queryLanguage,
                                 @NonNull final QueryScope queryScope,
                                 @NonNull final QueryTarget queryTarget)
            throws
            IOException,
            DapsTokenManagerException,
            MultipartParseException,
            ClaimsException,
            ShaclValidatorException,
            SerializeException,
            UnknownResponseException,
            SendMessageException,
            DeserializeException,
            RejectionException,
            UnexpectedPayloadException {
        return queryService
                .query(brokerURI, query, queryLanguage, queryScope, queryTarget);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MessageContainer<String> fullTextSearchBroker(final URI brokerURI,
                                          final String searchTerm,
                                          final QueryScope queryScope,
                                          final QueryTarget queryTarget)
            throws
            DapsTokenManagerException,
            IOException,
            MultipartParseException,
            ClaimsException,
            ShaclValidatorException,
            SerializeException,
            UnknownResponseException,
            SendMessageException,
            DeserializeException,
            RejectionException,
            UnexpectedPayloadException {
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
    public MessageContainer<String> fullTextSearchBroker(final URI brokerURI,
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
            DeserializeException,
            RejectionException,
            UnexpectedPayloadException {
        return queryService
                .fullTextSearch(brokerURI, searchTerm, queryScope,
                                queryTarget, limit, offset);
    }
}
