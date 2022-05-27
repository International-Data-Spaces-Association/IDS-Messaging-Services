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
package ids.messaging.protocol;

import java.io.IOException;
import java.net.URI;

import de.fraunhofer.iais.eis.QueryLanguage;
import de.fraunhofer.iais.eis.QueryScope;
import de.fraunhofer.iais.eis.QueryTarget;
import ids.messaging.common.DeserializeException;
import ids.messaging.common.SerializeException;
import ids.messaging.core.daps.ClaimsException;
import ids.messaging.core.daps.DapsTokenManagerException;
import ids.messaging.protocol.http.SendMessageException;
import ids.messaging.protocol.http.ShaclValidatorException;
import ids.messaging.protocol.multipart.UnknownResponseException;
import ids.messaging.protocol.multipart.parser.MultipartParseException;
import ids.messaging.requests.IDSInfrastructureService;
import ids.messaging.requests.MessageContainer;
import ids.messaging.requests.exceptions.RejectionException;
import ids.messaging.requests.exceptions.UnexpectedPayloadException;
import lombok.NonNull;

/**
 * Interface for query services.
 */
public interface IDSQueryService extends IDSInfrastructureService {
    /**
     * Builds and sends a {@link de.fraunhofer.iais.eis.QueryMessage}
     * to the Infrastructure Component.
     *
     * @param targetURI The URI of a Infrastructure Component.
     * @param query The query as payload for the QueryMessage.
     * @param queryLanguage The Language of the Query (e.g. SPARQL, SQL, XQUERY).
     *                      See {@link QueryLanguage}
     * @param queryScope The Scope of the Query (ALL connectors, ACTIVE connectors,
     *                   INACTIVE connectors). See {@link QueryScope}.
     * @param queryTarget The type of IDS Components that are queried. See {@link QueryTarget}.
     * @return The response to the query request.
     * @throws DapsTokenManagerException If no DAT for sending the message could be received.
     * @throws ClaimsException If DAT of incoming message could not be validated.
     * @throws MultipartParseException If response could not be parsed to header and payload.
     * @throws IOException Other errors, which were not categorized.
     * @throws ShaclValidatorException If the message does not pass the SHACL validation test.
     * @throws SerializeException If there are problems with serializing.
     * @throws UnknownResponseException If the format of the answer is not known.
     * @throws SendMessageException If there is an error when sending the request.
     * @throws DeserializeException If the deserialization of the received message fails.
     * @throws RejectionException When a RejectionMessage arrives unexpectedly.
     * @throws UnexpectedPayloadException When the payload cannot be used.
     */
    MessageContainer<String> query(@NonNull URI targetURI,
                                   @NonNull String query,
                                   @NonNull QueryLanguage queryLanguage,
                                   @NonNull QueryScope queryScope,
                                   @NonNull QueryTarget queryTarget)
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
            UnexpectedPayloadException;
    /**
     * Do a FullText Query on the Infrastructure Component with default limit and offset.
     *
     * @param targetURI The URI of a Infrastructure Component.
     * @param searchTerm The searchterm used in the query.
     * @param queryScope The Scope of the query.
     * @param queryTarget The target of the query.
     * @return The query result.
     * @throws DapsTokenManagerException If no DAT for sending the message could be received.
     * @throws ClaimsException If DAT of incoming message could not be validated.
     * @throws MultipartParseException If response could not be parsed to header and payload.
     * @throws IOException Other errors, which were not categorized.
     * @throws ShaclValidatorException If the message does not pass the SHACL validation test.
     * @throws SerializeException If there are problems with serializing.
     * @throws UnknownResponseException If the format of the answer is not known.
     * @throws SendMessageException If there is an error when sending the request.
     * @throws DeserializeException If the deserialization of the received message fails.
     * @throws RejectionException When a RejectionMessage arrives unexpectedly.
     * @throws UnexpectedPayloadException When the payload cannot be used.
     */
    MessageContainer<String> boundFullTextSearch(URI targetURI,
                                                 String searchTerm,
                                                 QueryScope queryScope,
                                                 QueryTarget queryTarget)
            throws
            IOException,
            MultipartParseException,
            ClaimsException,
            DapsTokenManagerException,
            ShaclValidatorException,
            SerializeException,
            UnknownResponseException,
            SendMessageException,
            DeserializeException,
            RejectionException,
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
     * @throws IOException Other errors, which were not categorized.
     * @throws MultipartParseException If the content cannot be parsed.
     * @throws ClaimsException If the claims cannot be successfully verified.
     * @throws DapsTokenManagerException If there are problems when retrieving the DAT
     * from the DAPS.
     * @throws ShaclValidatorException If the message does not pass the SHACL validation test.
     * @throws SerializeException If there are problems with serializing.
     * @throws UnknownResponseException If the format of the answer is not known.
     * @throws SendMessageException If there is an error when sending the request.
     * @throws DeserializeException If the deserialization of the received message fails.
     * @throws RejectionException When a RejectionMessage arrives unexpectedly.
     * @throws UnexpectedPayloadException When the payload cannot be used.
     */
    MessageContainer<String> fullTextSearch(URI targetURI,
                                            String searchTerm,
                                            QueryScope queryScope,
                                            QueryTarget queryTarget,
                                            int limit,
                                            int offset)
            throws
            IOException,
            MultipartParseException,
            ClaimsException,
            DapsTokenManagerException,
            ShaclValidatorException,
            SerializeException,
            UnknownResponseException,
            SendMessageException,
            DeserializeException,
            RejectionException,
            UnexpectedPayloadException;
}
