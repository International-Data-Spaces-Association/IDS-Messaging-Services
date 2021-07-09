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
package de.fraunhofer.ids.messaging.requests;

import java.io.IOException;
import java.net.URI;

import de.fraunhofer.iais.eis.QueryLanguage;
import de.fraunhofer.iais.eis.QueryScope;
import de.fraunhofer.iais.eis.QueryTarget;
import de.fraunhofer.ids.messaging.broker.util.FullTextQueryTemplate;
import de.fraunhofer.ids.messaging.common.DeserializeException;
import de.fraunhofer.ids.messaging.common.SerializeException;
import de.fraunhofer.ids.messaging.core.config.ConfigContainer;
import de.fraunhofer.ids.messaging.core.daps.ClaimsException;
import de.fraunhofer.ids.messaging.core.daps.DapsTokenManagerException;
import de.fraunhofer.ids.messaging.core.daps.DapsTokenProvider;
import de.fraunhofer.ids.messaging.protocol.IDSQueryService;
import de.fraunhofer.ids.messaging.protocol.MessageService;
import de.fraunhofer.ids.messaging.protocol.http.SendMessageException;
import de.fraunhofer.ids.messaging.protocol.http.ShaclValidatorException;
import de.fraunhofer.ids.messaging.protocol.multipart.UnknownResponseException;
import de.fraunhofer.ids.messaging.protocol.multipart.parser.MultipartParseException;
import de.fraunhofer.ids.messaging.requests.builder.IdsRequestBuilderService;
import de.fraunhofer.ids.messaging.requests.exceptions.RejectionException;
import de.fraunhofer.ids.messaging.requests.exceptions.UnexpectedPayloadException;
import lombok.NonNull;

public class QueryService extends InfrastructureService implements
        IDSQueryService {
    static int DEFAULT_LIMIT = 50;
    static int DEFAULT_OFFSET = 0;

    /**
     * QueryService constructor.
     * @param container the ConfigContainer
     * @param tokenProvider the DapsTokenProvider
     * @param messageService the MessageService
     */
    public QueryService(
            ConfigContainer container,
            DapsTokenProvider tokenProvider,
            MessageService messageService,
            IdsRequestBuilderService idsRequestBuilderService) {
        super(container, tokenProvider, messageService, idsRequestBuilderService);
    }

    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public MessageContainer<Object> query( @NonNull final URI targetURI,
                                           @NonNull final String query,
                                           @NonNull final QueryLanguage queryLanguage,
                                           @NonNull final QueryScope queryScope,
                                           @NonNull final QueryTarget queryTarget )
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
                .newRequest()
                .withPayload(query)
                .subjectQuery()
                .useMultipart()
                .operationSend(queryLanguage, queryScope, queryTarget)
                .execute(targetURI);
    }

    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public MessageContainer<Object> boundFullTextSearch( final URI targetURI,
                                                    final String searchTerm,
                                                    final QueryScope queryScope,
                                                    final QueryTarget queryTarget )
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
     * @return
     */
    @Override
    public MessageContainer<Object> fullTextSearch( final URI targetURI,
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
            ShaclValidatorException,
            SerializeException,
            UnknownResponseException,
            SendMessageException,
            DeserializeException,
            RejectionException,
            UnexpectedPayloadException {
        final var payload = String.format(
                FullTextQueryTemplate.FULL_TEXT_QUERY,
                searchTerm, limit, offset);
        return requestBuilderService
                .newRequest()
                .withPayload(payload)
                .subjectQuery()
                .useMultipart()
                .operationSend(QueryLanguage.SPARQL, queryScope, queryTarget)
                .execute(targetURI);
    }

}
