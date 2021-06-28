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
import java.util.List;

import de.fraunhofer.iais.eis.QueryLanguage;
import de.fraunhofer.iais.eis.QueryScope;
import de.fraunhofer.iais.eis.QueryTarget;
import de.fraunhofer.iais.eis.Resource;
import de.fraunhofer.ids.messaging.core.daps.ClaimsException;
import de.fraunhofer.ids.messaging.core.daps.ConnectorMissingCertExtensionException;
import de.fraunhofer.ids.messaging.core.daps.DapsConnectionException;
import de.fraunhofer.ids.messaging.core.daps.DapsEmptyResponseException;
import de.fraunhofer.ids.messaging.core.daps.DapsTokenManagerException;
import de.fraunhofer.ids.messaging.protocol.SerializeException;
import de.fraunhofer.ids.messaging.protocol.UnexpectedResponseException;
import de.fraunhofer.ids.messaging.common.DeserializeException;
import de.fraunhofer.ids.messaging.protocol.http.SendMessageException;
import de.fraunhofer.ids.messaging.protocol.http.ShaclValidatorException;
import de.fraunhofer.ids.messaging.protocol.multipart.UnknownResponseException;
import de.fraunhofer.ids.messaging.protocol.multipart.mapping.DescriptionResponseMAP;
import de.fraunhofer.ids.messaging.protocol.multipart.mapping.MessageProcessedNotificationMAP;
import de.fraunhofer.ids.messaging.protocol.multipart.mapping.ResultMAP;
import de.fraunhofer.ids.messaging.protocol.multipart.parser.MultipartParseException;

/**
 * Interface for Communication with IDS Brokers, implemented by {@link BrokerService}.
 */
public interface IDSBrokerService {

    /**
     *
     * @param uri  the URI of the Infrastructure Component
     * @return Response MAP with the SelfDescription in the payload as String
     * @throws DapsTokenManagerException  if no DAT for sending the message could be received.
     * @throws IOException  if message could not be sent or Serializer could not parse RDF to Java Object.
     * @throws ClaimsException  if DAT of incoming message could not be validated.
     * @throws MultipartParseException  if response could not be parsed to header and payload.
     */
    DescriptionResponseMAP requestSelfDescription(URI uri) throws
            DapsTokenManagerException,
            ClaimsException,
            MultipartParseException,
            IOException,
            UnknownResponseException,
            DeserializeException,
            SerializeException,
            UnexpectedResponseException,
            ShaclValidatorException,
            SendMessageException;

    /**
     * Builds and sends a {@link de.fraunhofer.iais.eis.ResourceUnavailableMessage} to the broker.
     * The given Resource will be unregistered from the broker.
     *
     * @param brokerURI URI of the broker the connector will try to unregister the resource at
     * @param resource  the resource that will be unregistered at the broker
     * @return the ResponseMessage of the Broker
     * @throws IOException if the built message could not be serialized
     */
    MessageProcessedNotificationMAP removeResourceFromBroker(URI brokerURI, Resource resource)
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
            SendMessageException;

    /**
     * Builds and sends a {@link de.fraunhofer.iais.eis.ConnectorUpdateMessage} to the broker.
     * The Connector will be registered at the broker, or its selfdescription will be updated.
     *
     * @param brokerURI URI of the broker the connector will try to unregister the resource at
     * @param resource  the resource that will be unregistered at the broker
     * @return the ResponseMessage of the Broker
     * @throws IOException if the built message could not be serialized
     */
    MessageProcessedNotificationMAP updateResourceAtBroker(URI brokerURI, Resource resource)
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
            SendMessageException;

    /**
     * Builds and sends a {@link de.fraunhofer.iais.eis.ConnectorUnavailableMessage} to the broker.
     * The Connector will be unregistered from the broker.
     *
     * @param brokerURI URI of the broker the connector will try to unregister at
     * @return the ResponseMessage of the Broker (NotificationMessage if it worked, RejectionMessage if not)
     * @throws IOException if the message could not be serialized
     */
    MessageProcessedNotificationMAP unregisterAtBroker(URI brokerURI)
            throws
            IOException,
            DapsTokenManagerException,
            ClaimsException,
            MultipartParseException,
            UnknownResponseException,
            DeserializeException,
            UnexpectedResponseException,
            SerializeException,
            ShaclValidatorException,
            SendMessageException;

    /**
     * Builds and sends a {@link de.fraunhofer.iais.eis.ConnectorUpdateMessage} to the broker.
     * The currently available connector self declaration at the broker will be updated. In order to update the
     * connector uuid in the self declaration has to be the same as the registered one at the broker.
     *
     * @param brokerURI URI of the broker the connector will try to update its information at
     * @return the ResponseMessage of the Broker (NotificationMessage if it worked, RejectionMessage if not)
     * @throws IOException if the built message could not be serialized
     */
    MessageProcessedNotificationMAP updateSelfDescriptionAtBroker(URI brokerURI)
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
            SendMessageException;

    /**
     * Builds and sends a {@link de.fraunhofer.iais.eis.ConnectorUpdateMessage} to a list of brokers.
     *
     * @param brokerURIs URIs of the brokers the connector will try to update its information at
     * @return a List of Responses from the Broker
     * @throws IOException if the built message could not be serialized
     */
    List<MessageProcessedNotificationMAP> updateSelfDescriptionAtBrokers(List<URI> brokerURIs)
            throws
            IOException,
            DapsTokenManagerException,
            DeserializeException,
            ShaclValidatorException,
            UnexpectedResponseException,
            SerializeException,
            MultipartParseException,
            ClaimsException,
            SendMessageException,
            UnknownResponseException;

    /**
     * Builds and sends a {@link de.fraunhofer.iais.eis.QueryMessage} to the broker.
     *
     * @param brokerURI     the URI of the broker the message is sent to
     * @param query         the query as payload for the QueryMessage
     * @param queryLanguage the Language of the Query (e.g. SPARQL, SQL, XQUERY). See {@link QueryLanguage}
     * @param queryScope    the Scope of the Query (ALL connectors, ACTIVE connectors, INACTIVE connectors). See {@link QueryScope}
     * @param queryTarget   the type of IDS Components that are queried. See {@link QueryTarget}
     * @return the brokers response to the query request
     * @throws IOException if the built message could not be serialized
     */
    ResultMAP queryBroker(URI brokerURI, String query, QueryLanguage queryLanguage, QueryScope queryScope, QueryTarget queryTarget)
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
            SendMessageException;

    /**
     * Do a FullText Query on the Broker with default limit and offset.
     *
     * @param brokerURI The URI of the Broker.
     * @param searchTerm The searchterm used in the query.
     * @param queryScope The Scope of the query.
     * @param queryTarget The target of the query.
     * @return The query result from the Broker.
     * @throws ConnectorMissingCertExtensionException Exception while getting DAT from DAPS.
     * @throws DapsConnectionException Exception while getting DAT from DAPS.
     * @throws DapsEmptyResponseException Exception while getting DAT from DAPS.
     * @throws IOException Exception while getting DAT from DAPS.
     * @throws MultipartParseException Exception while parsing the response.
     * @throws ClaimsException Exception while validating the DAT from the Broker Response.
     */
    ResultMAP fullTextSearchBroker(URI brokerURI, String searchTerm, QueryScope queryScope,
                                   QueryTarget queryTarget)
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
            SendMessageException;

    /**
     * Do a FullText Query on the Broker with custom limit and offset.
     *
     * @param brokerURI The URI of the Broker.
     * @param searchTerm The searchterm used in the query.
     * @param queryScope The Scope of the query.
     * @param queryTarget The target of the query.
     * @param limit Custom limit used in the query.
     * @param offset Custom offset used in the query.
     * @return The query result from the Broker.
     * @throws ConnectorMissingCertExtensionException Exception while getting DAT from DAPS.
     * @throws DapsConnectionException Exception while getting DAT from DAPS.
     * @throws DapsEmptyResponseException Exception while getting DAT from DAPS.
     * @throws IOException Exception while getting DAT from DAPS.
     * @throws MultipartParseException Exception while parsing the response.
     * @throws ClaimsException Exception while validating the DAT from the Broker Response.
     */
    ResultMAP fullTextSearchBroker(URI brokerURI, String searchTerm, QueryScope queryScope,
                                   QueryTarget queryTarget, int limit, int offset )
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
            SendMessageException;
}
