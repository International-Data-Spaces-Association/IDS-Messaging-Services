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
