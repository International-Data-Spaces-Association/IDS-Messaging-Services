package de.fraunhofer.ids.messaging.broker;

import java.net.URI;

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
     * @param securityToken    the DAT Token used for this request
     * @param infoModelVersion the Infomodel Version of the connector
     * @param connectorID      the ID of the connector
     * @param resource         the resource that is marked as unavailable at the broker
     * @return the {@link ResourceUnavailableMessage}
     * @throws ConstraintViolationException when the message cannot be built properly
     */
    public ResourceUnavailableMessage buildResourceUnavailableMessage(final DynamicAttributeToken securityToken,
                                                                      final String infoModelVersion,
                                                                      final URI connectorID,
                                                                      final Resource resource)
            throws ConstraintViolationException {
        return new ResourceUnavailableMessageBuilder()
                ._affectedResource_(resource.getId())
                ._securityToken_(securityToken)
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._senderAgent_(connectorID)
                ._issuerConnector_(connectorID)
                ._modelVersion_(infoModelVersion)
                .build();
    }

    /**
     * Create a ResourceUpdateMessage used for registering and updating the given resource at a broker.
     *
     * @param securityToken    the DAT Token used for this request
     * @param infoModelVersion the Infomodel Version of the connector
     * @param connectorID      the ID of the connector
     * @param resource         the resource that is updated at the broker
     * @return the {@link ResourceUpdateMessage} as JSONLD
     * @throws ConstraintViolationException when the message cannot be built properly
     */
    public ResourceUpdateMessage buildResourceUpdateMessage(final DynamicAttributeToken securityToken,
                                                            final String infoModelVersion,
                                                            final URI connectorID,
                                                            final Resource resource)
            throws ConstraintViolationException {
        return new ResourceUpdateMessageBuilder()
                ._affectedResource_(resource.getId())
                ._securityToken_(securityToken)
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._senderAgent_(connectorID)
                ._issuerConnector_(connectorID)
                ._modelVersion_(infoModelVersion)
                .build();
    }

    /**
     * Create a ConnectorUnavailableMessage used for unregistering the connector at a broker.
     *
     * @param securityToken    the DAT Token used for this request
     * @param infoModelVersion the Infomodel Version of the connector
     * @param connectorID      the ID of the connector
     * @return the {@link ConnectorUnavailableMessage}
     * @throws ConstraintViolationException when the message cannot be built properly
     */
    public ConnectorUnavailableMessage buildUnavailableMessage(final DynamicAttributeToken securityToken,
                                                               final String infoModelVersion,
                                                               final URI connectorID)
            throws ConstraintViolationException {
        return new ConnectorUnavailableMessageBuilder()
                ._securityToken_(securityToken)
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._modelVersion_(infoModelVersion)
                ._issuerConnector_(connectorID)
                ._senderAgent_(connectorID)
                ._affectedConnector_(connectorID)
                .build();
    }

    /**
     * Create a ConnectorUpdateMessage used for registering the connector at a broker.
     *
     * @param securityToken    the DAT Token used for this request
     * @param infoModelVersion the Infomodel Version of the connector
     * @param connectorID      the ID of the connector
     * @return the {@link ConnectorUpdateMessage}
     * @throws ConstraintViolationException when the message cannot be serialized properly
     */
    public ConnectorUpdateMessage buildUpdateMessage(final DynamicAttributeToken securityToken,
                                                     final String infoModelVersion,
                                                     final URI connectorID)
            throws ConstraintViolationException {
        return new ConnectorUpdateMessageBuilder()
                ._securityToken_(securityToken)
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._modelVersion_(infoModelVersion)
                ._issuerConnector_(connectorID)
                ._senderAgent_(connectorID)
                ._affectedConnector_(connectorID)
                .build();
    }

    /**
     * Create a QueryMessage used for querying the broker.
     *
     * @param securityToken    the DAT Token used for this request
     * @param infoModelVersion the Infomodel Version of the connector
     * @param connectorID      the ID of the connector
     * @param queryLanguage    the Language of the Query (e.g. SPARQL, SQL, XQUERY)
     * @param queryScope       the Scope of the Query (ALL connectors, ACTIVE connectors, INACTIVE connectors) {@link QueryScope}
     * @param queryTarget      the type of IDS Components that are queried {@link QueryTarget}
     * @return the {@link QueryMessage}
     * @throws ConstraintViolationException when the message cannot be built properly
     */
    public QueryMessage buildQueryMessage(final DynamicAttributeToken securityToken,
                                          final String infoModelVersion,
                                          final URI connectorID,
                                          final QueryLanguage queryLanguage,
                                          final QueryScope queryScope,
                                          final QueryTarget queryTarget)
            throws ConstraintViolationException {
        return new QueryMessageBuilder()
                ._securityToken_(securityToken)
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._modelVersion_(infoModelVersion)
                ._issuerConnector_(connectorID)
                ._senderAgent_(connectorID)
                ._queryLanguage_(queryLanguage)
                ._queryScope_(queryScope)
                ._recipientScope_(queryTarget)
                .build();
    }
}