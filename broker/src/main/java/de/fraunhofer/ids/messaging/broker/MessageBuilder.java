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

import de.fraunhofer.iais.eis.Connector;
import de.fraunhofer.iais.eis.ConnectorUnavailableMessage;
import de.fraunhofer.iais.eis.ConnectorUnavailableMessageBuilder;
import de.fraunhofer.iais.eis.ConnectorUpdateMessage;
import de.fraunhofer.iais.eis.ConnectorUpdateMessageBuilder;
import de.fraunhofer.iais.eis.DynamicAttributeToken;
import de.fraunhofer.iais.eis.QueryLanguage;
import de.fraunhofer.iais.eis.QueryMessage;
import de.fraunhofer.iais.eis.QueryMessageBuilder;
import de.fraunhofer.iais.eis.QueryScope;
import de.fraunhofer.iais.eis.QueryTarget;
import de.fraunhofer.iais.eis.Resource;
import de.fraunhofer.iais.eis.ResourceUnavailableMessage;
import de.fraunhofer.iais.eis.ResourceUnavailableMessageBuilder;
import de.fraunhofer.iais.eis.ResourceUpdateMessage;
import de.fraunhofer.iais.eis.ResourceUpdateMessageBuilder;
import de.fraunhofer.iais.eis.util.ConstraintViolationException;
import de.fraunhofer.ids.messaging.common.MessageBuilderException;
import de.fraunhofer.ids.messaging.util.IdsMessageUtils;
import lombok.experimental.UtilityClass;

/**
 * The MessageUtils class contains utility methods for building Infomodel Messages (used by the {@link BrokerService} class).
 */
@UtilityClass
public class MessageBuilder {
    /**
     * Create a ResourceUnavailableMessage used for unregistering the given resource at a broker.
     *
     * @param securityToken the DAT Token used for this request
     * @param connector     the connector from which the message is sent
     * @param resource      the resource that is marked as unavailable at the broker
     *
     * @return the {@link ResourceUnavailableMessage}
     *
     * @throws ConstraintViolationException when the message cannot be built properly
     */
    public static ResourceUnavailableMessage buildResourceUnavailableMessage(final DynamicAttributeToken securityToken,
                                                                             final Connector connector,
                                                                             final Resource resource) throws
            MessageBuilderException {
        try {
            return new ResourceUnavailableMessageBuilder()
                    ._affectedResource_(resource.getId())
                    ._securityToken_(securityToken)
                    ._issued_(IdsMessageUtils.getGregorianNow())
                    ._senderAgent_(connector.getId())
                    ._issuerConnector_(connector.getId())
                    ._modelVersion_(connector.getOutboundModelVersion())
                    .build();
        } catch (ConstraintViolationException constraintViolationException) {
            throw new MessageBuilderException(constraintViolationException);
        }
    }

    /**
     * Create a ResourceUpdateMessage used for registering and updating the given resource at a broker.
     *
     * @param securityToken the DAT Token used for this request
     * @param connector     the connector from which the message is sent
     * @param resource      the resource that is updated at the broker
     *
     * @return the {@link ResourceUpdateMessage} as JSONLD
     *
     * @throws ConstraintViolationException when the message cannot be built properly
     */
    public static ResourceUpdateMessage buildResourceUpdateMessage(final DynamicAttributeToken securityToken,
                                                                   final Connector connector,
                                                                   final Resource resource) throws MessageBuilderException {
        try {
            return new ResourceUpdateMessageBuilder()
                    ._affectedResource_(resource.getId())
                    ._securityToken_(securityToken)
                    ._issued_(IdsMessageUtils.getGregorianNow())
                    ._senderAgent_(connector.getId())
                    ._issuerConnector_(connector.getId())
                    ._modelVersion_(connector.getOutboundModelVersion())
                    .build();
        } catch (ConstraintViolationException constraintViolationException) {
            throw new MessageBuilderException(constraintViolationException);
        }
    }

    /**
     * Create a ConnectorUnavailableMessage used for unregistering the connector at a broker.
     *
     * @param securityToken the DAT Token used for this request
     * @param connector     the connector from which the message is sent
     *
     * @return the {@link ConnectorUnavailableMessage}
     *
     * @throws ConstraintViolationException when the message cannot be built properly
     */
    public static ConnectorUnavailableMessage buildUnavailableMessage(final DynamicAttributeToken securityToken,
                                                                      final Connector connector) throws MessageBuilderException {
        try {
            return new ConnectorUnavailableMessageBuilder()
                    ._securityToken_(securityToken)
                    ._issued_(IdsMessageUtils.getGregorianNow())
                    ._modelVersion_(connector.getOutboundModelVersion())
                    ._issuerConnector_(connector.getId())
                    ._senderAgent_(connector.getId())
                    ._affectedConnector_(connector.getId())
                    .build();
        } catch (ConstraintViolationException constraintViolationException) {
            throw new MessageBuilderException(constraintViolationException);
        }
    }

    /**
     * Create a ConnectorUpdateMessage used for registering the connector at a broker.
     *
     * @param securityToken the DAT Token used for this request
     * @param connector     the connector from which the message is sent
     *
     * @return the {@link ConnectorUpdateMessage}
     *
     * @throws ConstraintViolationException when the message cannot be serialized properly
     */
    public static ConnectorUpdateMessage buildUpdateMessage(final DynamicAttributeToken securityToken,
                                                            final Connector connector) throws MessageBuilderException {
        try {
            return new ConnectorUpdateMessageBuilder()
                    ._securityToken_(securityToken)
                    ._issued_(IdsMessageUtils.getGregorianNow())
                    ._modelVersion_(connector.getOutboundModelVersion())
                    ._issuerConnector_(connector.getId())
                    ._senderAgent_(connector.getId())
                    ._affectedConnector_(connector.getId())
                    .build();
        } catch (ConstraintViolationException constraintViolationException) {
            throw new MessageBuilderException(constraintViolationException);
        }
    }

    /**
     * Create a QueryMessage used for querying the broker.
     *
     * @param securityToken the DAT Token used for this request
     * @param connector     the connector from which the message is sent
     * @param queryLanguage the Language of the Query (e.g. SPARQL, SQL, XQUERY)
     * @param queryScope    the Scope of the Query (ALL connectors, ACTIVE connectors, INACTIVE connectors) {@link QueryScope}
     * @param queryTarget   the type of IDS Components that are queried {@link QueryTarget}
     *
     * @return the {@link QueryMessage}
     *
     * @throws ConstraintViolationException when the message cannot be built properly
     */
    public static QueryMessage buildQueryMessage(final DynamicAttributeToken securityToken,
                                                 final Connector connector,
                                                 final QueryLanguage queryLanguage,
                                                 final QueryScope queryScope,
                                                 final QueryTarget queryTarget) throws MessageBuilderException {
        try {
            return new QueryMessageBuilder()
                    ._securityToken_(securityToken)
                    ._issued_(IdsMessageUtils.getGregorianNow())
                    ._modelVersion_(connector.getOutboundModelVersion())
                    ._issuerConnector_(connector.getId())
                    ._senderAgent_(connector.getId())
                    ._queryLanguage_(queryLanguage)
                    ._queryScope_(queryScope)
                    ._recipientScope_(queryTarget)
                    .build();
        } catch (ConstraintViolationException constraintViolationException) {
            throw new MessageBuilderException(constraintViolationException);
        }
    }
}
