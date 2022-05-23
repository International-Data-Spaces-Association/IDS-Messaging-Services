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
package ids.messaging.requests;

import java.io.IOException;
import java.net.URI;

import de.fraunhofer.iais.eis.QueryLanguage;
import de.fraunhofer.iais.eis.QueryScope;
import de.fraunhofer.iais.eis.QueryTarget;
import ids.messaging.util.FullTextQueryTemplate;
import ids.messaging.common.DeserializeException;
import ids.messaging.common.SerializeException;
import ids.messaging.core.config.ConfigContainer;
import ids.messaging.core.daps.ClaimsException;
import ids.messaging.core.daps.DapsTokenManagerException;
import ids.messaging.core.daps.DapsTokenProvider;
import ids.messaging.protocol.IDSQueryService;
import ids.messaging.protocol.MessageService;
import ids.messaging.protocol.http.SendMessageException;
import ids.messaging.protocol.http.ShaclValidatorException;
import ids.messaging.protocol.multipart.UnknownResponseException;
import ids.messaging.protocol.multipart.parser.MultipartParseException;
import ids.messaging.requests.builder.IdsRequestBuilderService;
import ids.messaging.requests.exceptions.RejectionException;
import ids.messaging.requests.exceptions.UnexpectedPayloadException;
import lombok.NonNull;

/**
 * Service class to build Query-Messages (e.g. for Broker and Vocol).
 */
public class QueryService extends InfrastructureService implements
        IDSQueryService {
    /**
     * The default limit for the query.
     */
    private static final int DEFAULT_LIMIT = 50;

    /**
     * The default offset for the query.
     */
    private static final int DEFAULT_OFFSET = 0;

    /**
     * QueryService constructor.
     *
     * @param container The ConfigContainer.
     * @param tokenProvider The DapsTokenProvider.
     * @param messageService The MessageService.
     * @param idsRequestBuilderService The IdsRequestBuilderService.
     */
    public QueryService(
            final ConfigContainer container,
            final DapsTokenProvider tokenProvider,
            final MessageService messageService,
            final IdsRequestBuilderService idsRequestBuilderService) {
        super(container, tokenProvider, messageService, idsRequestBuilderService);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MessageContainer<String> query(@NonNull final URI targetURI,
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
            DeserializeException,
            RejectionException,
            UnexpectedPayloadException {
        super.logBuildingHeader();
        return requestBuilderService
                .newRequestExpectingType(String.class)
                .withPayload(query)
                .subjectQuery()
                .useMultipart()
                .operationSend(queryLanguage, queryScope, queryTarget)
                .execute(targetURI);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MessageContainer<String> boundFullTextSearch(final URI targetURI,
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
        return fullTextSearch(targetURI,
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
    public MessageContainer<String> fullTextSearch(final URI targetURI,
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

        //Check whether the search term has already been entered in
        //quotation marks, if so, these must be removed
        var serviceSearchTerm = searchTerm;
        if (searchTerm.length() >= 2) {
            final var firstChar = searchTerm.charAt(0);
            final var lastChar = searchTerm.charAt(searchTerm.length() - 1);
            if (firstChar == '"' && lastChar == '"') {
                serviceSearchTerm = searchTerm.substring(1, searchTerm.length() - 1);
            }
        }

        final var payload = String.format(
                FullTextQueryTemplate.FULL_TEXT_QUERY,
                serviceSearchTerm, limit, offset);
        return requestBuilderService
                .newRequestExpectingType(String.class)
                .withPayload(payload)
                .subjectQuery()
                .useMultipart()
                .operationSend(QueryLanguage.SPARQL, queryScope, queryTarget)
                .execute(targetURI);
    }
}
