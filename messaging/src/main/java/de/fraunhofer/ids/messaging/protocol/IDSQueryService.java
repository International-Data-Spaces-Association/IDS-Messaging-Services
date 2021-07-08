package de.fraunhofer.ids.messaging.protocol;

import java.io.IOException;
import java.net.URI;

import de.fraunhofer.iais.eis.QueryLanguage;
import de.fraunhofer.iais.eis.QueryScope;
import de.fraunhofer.iais.eis.QueryTarget;
import de.fraunhofer.ids.messaging.common.DeserializeException;
import de.fraunhofer.ids.messaging.common.SerializeException;
import de.fraunhofer.ids.messaging.core.daps.*;
import de.fraunhofer.ids.messaging.protocol.http.SendMessageException;
import de.fraunhofer.ids.messaging.protocol.http.ShaclValidatorException;
import de.fraunhofer.ids.messaging.protocol.multipart.UnknownResponseException;
import de.fraunhofer.ids.messaging.protocol.multipart.parser.MultipartParseException;
import de.fraunhofer.ids.messaging.requests.IDSInfrastructureService;
import de.fraunhofer.ids.messaging.requests.MessageContainer;
import de.fraunhofer.ids.messaging.requests.exceptions.RejectionException;
import de.fraunhofer.ids.messaging.requests.exceptions.UnexpectedPayloadException;
import lombok.NonNull;

public interface IDSQueryService extends IDSInfrastructureService {
    /**
     * Builds and sends a {@link de.fraunhofer.iais.eis.QueryMessage} to the Infrastructure Component.
     *
     * @param targetURI     The URI of a Infrastructure Component.
     * @param query         the query as payload for the QueryMessage
     * @param queryLanguage the Language of the Query (e.g. SPARQL, SQL, XQUERY). See {@link QueryLanguage}
     * @param queryScope    the Scope of the Query (ALL connectors, ACTIVE connectors, INACTIVE connectors). See {@link QueryScope}
     * @param queryTarget   the type of IDS Components that are queried. See {@link QueryTarget}
     * @return the response to the query request
     * @throws ConnectorMissingCertExtensionException Exception while getting DAT from DAPS.
     * @throws DapsConnectionException Exception while getting DAT from DAPS.
     * @throws DapsEmptyResponseException Exception while getting DAT from DAPS.
     * @throws IOException Exception while getting DAT from DAPS.
     * @throws MultipartParseException Exception while parsing the response.
     * @throws ClaimsException Exception while validating the DAT from the Response.
     */
    MessageContainer<Object> query( @NonNull URI targetURI,
                                    @NonNull String query,
                                    @NonNull QueryLanguage queryLanguage,
                                    @NonNull QueryScope queryScope,
                                    @NonNull QueryTarget queryTarget )
            throws
            IOException,
            DapsTokenManagerException,
            MultipartParseException,
            ClaimsException, ShaclValidatorException, SerializeException,
            UnknownResponseException, SendMessageException,
            DeserializeException, RejectionException,
            UnexpectedPayloadException;
    /**
     * Do a FullText Query on the Infrastructure Component with default limit and offset.
     *
     * @param targetURI The URI of a Infrastructure Component.
     * @param searchTerm The searchterm used in the query.
     * @param queryScope The Scope of the query.
     * @param queryTarget The target of the query.
     * @return The query result.
     * @throws ConnectorMissingCertExtensionException Exception while getting DAT from DAPS.
     * @throws DapsConnectionException Exception while getting DAT from DAPS.
     * @throws DapsEmptyResponseException Exception while getting DAT from DAPS.
     * @throws IOException Exception while getting DAT from DAPS.
     * @throws MultipartParseException Exception while parsing the response.
     * @throws ClaimsException Exception while validating the DAT from the Response.
     */
    MessageContainer<Object> boundFullTextSearch( URI targetURI,
                                             String searchTerm,
                                             QueryScope queryScope,
                                             QueryTarget queryTarget )
            throws
            IOException,
            MultipartParseException,
            ClaimsException, DapsTokenManagerException, ShaclValidatorException,
            SerializeException, UnknownResponseException, SendMessageException,
            DeserializeException, RejectionException,
            UnexpectedPayloadException;

    /**
     * Do a FullText Query on the Infrastructure Component with custom limit and offset.
     *
     * @param targetURI The URI of a Infrastructure Component.
     * @param searchTerm The searchterm used in the query.
     * @param queryScope The Scope of the query.
     * @param queryTarget The target of the query.
     * @param limit Custom limit used in the query.
     * @param offset Custom offset used in the query.
     * @return The query result.
     * @throws ConnectorMissingCertExtensionException Exception while getting DAT from DAPS.
     * @throws DapsConnectionException Exception while getting DAT from DAPS.
     * @throws DapsEmptyResponseException Exception while getting DAT from DAPS.
     * @throws IOException Exception while getting DAT from DAPS.
     * @throws MultipartParseException Exception while parsing the response.
     * @throws ClaimsException Exception while validating the DAT from the Response.
     */
    MessageContainer<Object> fullTextSearch( URI targetURI,
                                             String searchTerm,
                                             QueryScope queryScope,
                                             QueryTarget queryTarget,
                                             int limit,
                                             int offset )
            throws
            IOException,
            MultipartParseException,
            ClaimsException, DapsTokenManagerException, ShaclValidatorException,
            SerializeException, UnknownResponseException, SendMessageException,
            DeserializeException, RejectionException,
            UnexpectedPayloadException;
}
