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
import ids.messaging.core.daps.ClaimsException;
import ids.messaging.core.daps.DapsTokenManagerException;
import ids.messaging.protocol.http.SendMessageException;
import ids.messaging.protocol.http.ShaclValidatorException;
import ids.messaging.protocol.multipart.UnknownResponseException;
import ids.messaging.protocol.multipart.parser.MultipartParseException;
import ids.messaging.requests.MessageContainer;
import ids.messaging.requests.exceptions.NoTemplateProvidedException;
import ids.messaging.requests.exceptions.RejectionException;
import ids.messaging.requests.exceptions.UnexpectedPayloadException;

/**
 * Interface for Communication with IDS Brokers, implemented by {@link BrokerService}.
 */
public interface IDSBrokerService {

    /**
     * Builds and sends a {@link de.fraunhofer.iais.eis.ResourceUnavailableMessage} to the broker.
     * The given Resource will be unregistered from the broker.
     *
     * @param brokerURI URI of the broker the connector will try to unregister the resource at.
     * @param resource The resource that will be unregistered at the broker.
     * @return The ResponseMessage of the Broker.
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
     * @throws NoTemplateProvidedException If no internal template can be found for the message.
     */
    MessageContainer<?> removeResourceFromBroker(URI brokerURI, Resource resource)
            throws
            IOException,
            DapsTokenManagerException,
            MultipartParseException,
            ClaimsException,
            NoTemplateProvidedException,
            ShaclValidatorException,
            SerializeException,
            UnknownResponseException,
            SendMessageException,
            DeserializeException,
            RejectionException,
            UnexpectedPayloadException;

    /**
     * Builds and sends a {@link de.fraunhofer.iais.eis.ConnectorUpdateMessage} to the broker.
     * The Connector will be registered at the broker, or
     * its selfdescription will be updated.
     *
     * @param brokerURI URI of the broker the connector will try to unregister the resource at.
     * @param resource The resource that will be unregistered at the broker.
     * @return The ResponseMessage of the Broker.
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
     * @throws NoTemplateProvidedException If no internal template can be found for the message.
     */
    MessageContainer<?> updateResourceAtBroker(URI brokerURI, Resource resource)
            throws
            IOException,
            DapsTokenManagerException,
            MultipartParseException,
            ClaimsException,
            NoTemplateProvidedException,
            ShaclValidatorException,
            SerializeException,
            UnknownResponseException,
            SendMessageException,
            DeserializeException,
            RejectionException,
            UnexpectedPayloadException;

    /**
     * Builds and sends a
     * {@link de.fraunhofer.iais.eis.ConnectorUnavailableMessage} to the broker.
     * The Connector will be unregistered from the broker.
     *
     * @param brokerURI URI of the broker the connector will try to unregister at.
     * @return The ResponseMessage of the Broker (NotificationMessage
     * if it worked, RejectionMessage if not).
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
     * @throws NoTemplateProvidedException If no internal template can be found for the message.
     */
    MessageContainer<?> unregisterAtBroker(URI brokerURI)
            throws IOException,
            DapsTokenManagerException,
            ClaimsException,
            MultipartParseException,
            NoTemplateProvidedException,
            ShaclValidatorException,
            SerializeException,
            UnknownResponseException,
            SendMessageException,
            DeserializeException,
            RejectionException,
            UnexpectedPayloadException;

    /**
     * Builds and sends a {@link de.fraunhofer.iais.eis.ConnectorUpdateMessage}
     * to the broker.
     * The currently available connector self declaration at the
     * broker will be updated. In order to update the
     * connector uuid in the self declaration has to be the same
     * as the registered one at the broker.
     *
     * @param brokerURI URI of the broker the connector will try to update its information at.
     * @return The ResponseMessage of the Broker (NotificationMessage if it worked,
     * RejectionMessage if not).
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
     * @throws NoTemplateProvidedException If no internal template can be found for the message.
     */
    MessageContainer<?> updateSelfDescriptionAtBroker(URI brokerURI)
            throws IOException,
            DapsTokenManagerException,
            MultipartParseException,
            ClaimsException,
            NoTemplateProvidedException,
            ShaclValidatorException,
            SerializeException,
            UnknownResponseException,
            SendMessageException,
            DeserializeException,
            RejectionException,
            UnexpectedPayloadException;

    /**
     * Builds and sends a {@link de.fraunhofer.iais.eis.QueryMessage}
     * to the broker.
     *
     * @param brokerURI The URI of the broker the message is sent to.
     * @param query The query as payload for the QueryMessage.
     * @param queryLanguage The Language of the Query (e.g. SPARQL, SQL, XQUERY).
     *                      See {@link QueryLanguage}
     * @param queryScope The Scope of the Query (ALL connectors, ACTIVE connectors,
     *                   INACTIVE connectors). See {@link QueryScope}.
     * @param queryTarget The type of IDS Components that are queried. See {@link QueryTarget}.
     * @return The brokers response to the query request.
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
     * @throws NoTemplateProvidedException If no internal template can be found for the message.
     */
    MessageContainer<String> queryBroker(URI brokerURI,
                                         String query,
                                         QueryLanguage queryLanguage,
                                         QueryScope queryScope,
                                         QueryTarget queryTarget)
            throws IOException,
            DapsTokenManagerException,
            MultipartParseException,
            ClaimsException,
            NoTemplateProvidedException,
            ShaclValidatorException,
            SerializeException,
            UnknownResponseException,
            SendMessageException,
            DeserializeException,
            RejectionException,
            UnexpectedPayloadException;

    /**
     * Do a FullText Query on the Broker with default limit and offset.
     *
     * @param brokerURI The URI of the Broker.
     * @param searchTerm The searchterm used in the query.
     * @param queryScope The Scope of the query.
     * @param queryTarget The target of the query.
     * @return The query result from the Broker.
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
     * @throws NoTemplateProvidedException If no internal template can be found for the message.
     */
    MessageContainer<String> fullTextSearchBroker(URI brokerURI,
                                                  String searchTerm,
                                                  QueryScope queryScope,
                                                  QueryTarget queryTarget)
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
            DeserializeException,
            RejectionException,
            UnexpectedPayloadException;

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
     * @throws NoTemplateProvidedException If no internal template can be found for the message.
     */
    MessageContainer<String> fullTextSearchBroker(URI brokerURI,
                                                  String searchTerm,
                                                  QueryScope queryScope,
                                                  QueryTarget queryTarget,
                                                  int limit,
                                                  int offset)
            throws
            DapsTokenManagerException,
            IOException,
            MultipartParseException,
            NoTemplateProvidedException,
            ClaimsException,
            ShaclValidatorException,
            SerializeException,
            UnknownResponseException,
            SendMessageException,
            DeserializeException,
            RejectionException,
            UnexpectedPayloadException;
}
