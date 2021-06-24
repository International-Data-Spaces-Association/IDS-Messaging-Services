package de.fraunhofer.ids.messaging.protocol;

import java.io.IOException;
import java.net.URI;

import de.fraunhofer.iais.eis.QueryLanguage;
import de.fraunhofer.iais.eis.QueryScope;
import de.fraunhofer.iais.eis.QueryTarget;
import de.fraunhofer.ids.messaging.core.config.ConfigContainer;
import de.fraunhofer.ids.messaging.core.daps.*;
import de.fraunhofer.ids.messaging.protocol.multipart.MessageBuilder;
import de.fraunhofer.ids.messaging.protocol.multipart.mapping.GenericMessageAndPayload;
import de.fraunhofer.ids.messaging.protocol.multipart.mapping.ResultMAP;
import de.fraunhofer.ids.messaging.protocol.multipart.parser.MultipartParseException;
import de.fraunhofer.ids.messaging.util.FullTextQueryTemplate;
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
            MessageService messageService ) {
        super(container, tokenProvider, messageService);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResultMAP query( @NonNull final URI targetURI,
                            @NonNull final String query,
                            @NonNull final QueryLanguage queryLanguage,
                            @NonNull final QueryScope queryScope,
                            @NonNull final QueryTarget queryTarget )
            throws IOException, DapsTokenManagerException, MultipartParseException, ClaimsException {
        super.logBuildingHeader();

        final var securityToken = getDat();
        final var header = MessageBuilder.buildQueryMessage(
                securityToken,
                container.getConnector(),
                queryLanguage,
                queryScope,
                queryTarget);
        final var messageAndPayload = new GenericMessageAndPayload(header, query);
        final var response = messageService.sendIdsMessage(messageAndPayload,
                                                           targetURI);

        return expectResultMAP(response);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResultMAP fullTextSearch( final URI targetURI,
                                     final String searchTerm,
                                     final QueryScope queryScope,
                                     final QueryTarget queryTarget )
            throws
            ConnectorMissingCertExtensionException,
            DapsConnectionException,
            DapsEmptyResponseException,
            IOException,
            MultipartParseException,
            ClaimsException {
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
    public ResultMAP fullTextSearch( final URI targetURI,
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
            ClaimsException {
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
        final var response = messageService.sendIdsMessage(messageAndPayload,
                                                           targetURI);

        return expectResultMAP(response);
    }

}
