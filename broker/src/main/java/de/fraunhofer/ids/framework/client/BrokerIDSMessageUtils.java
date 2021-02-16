package de.fraunhofer.ids.framework.client;

import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.ids.framework.messaging.util.IdsMessageUtils;
import okhttp3.MultipartBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;

/**
 * The MessageUtils class contains utility methods for building Infomodel Messages (used by the {@link IDSBrokerServiceImpl} class)
 */
public class BrokerIDSMessageUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(BrokerIDSMessageUtils.class);

    private static final Serializer SERIALIZER = new Serializer();

    /**
     * Create a ResourceUnavailableMessage used for unregistering the given resource at a broker
     *
     * @param securityToken the DAT Token used for this request
     * @param INFO_MODEL_VERSION the Infomodel Version of the connector
     * @param connectorID the ID of the connector
     * @param resource the resource that is marked as unavailable at the broker
     * @return the {@link ResourceUnavailableMessage} as JSONLD
     * @throws IOException when the message cannot be serialized properly
     */
    public static String buildResourceUnavailableMessage(DynamicAttributeToken securityToken, String INFO_MODEL_VERSION, URI connectorID, Resource resource) throws IOException {
        var msg = new ResourceUnavailableMessageBuilder()
                ._affectedResource_(resource.getId())
                ._securityToken_(securityToken)
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._senderAgent_(connectorID)
                ._issuerConnector_(connectorID)
                ._modelVersion_(INFO_MODEL_VERSION)
                .build();
        return SERIALIZER.serialize(msg);
    }

    /**
     * Create a ResourceUpdateMessage used for registering and updating the given resource at a broker
     *
     * @param securityToken the DAT Token used for this request
     * @param INFO_MODEL_VERSION the Infomodel Version of the connector
     * @param connectorID the ID of the connector
     * @param resource the resource that is updated at the broker
     * @return the {@link ResourceUpdateMessage} as JSONLD
     * @throws IOException when the message cannot be serialized properly
     */
    public static String buildResourceUpdateMessage(DynamicAttributeToken securityToken, String INFO_MODEL_VERSION, URI connectorID, Resource resource) throws IOException {
        var msg = new ResourceUpdateMessageBuilder()
                ._affectedResource_(resource.getId())
                ._securityToken_(securityToken)
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._senderAgent_(connectorID)
                ._issuerConnector_(connectorID)
                ._modelVersion_(INFO_MODEL_VERSION)
                .build();
        return SERIALIZER.serialize(msg);
    }

    /**
     * Create a ConnectorUnavailableMessage used for unregistering the connector at a broker
     *
     * @param securityToken the DAT Token used for this request
     * @param INFO_MODEL_VERSION the Infomodel Version of the connector
     * @param connectorID the ID of the connector
     * @return the {@link ConnectorUnavailableMessage} as JSONLD
     * @throws IOException when the message cannot be serialized properly
     */
    public static String buildUnavailableMessage(DynamicAttributeToken securityToken, String INFO_MODEL_VERSION, URI connectorID) throws IOException {
        var msg = new ConnectorUnavailableMessageBuilder()
                ._securityToken_(securityToken)
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._modelVersion_(INFO_MODEL_VERSION)
                ._issuerConnector_(connectorID)
                ._senderAgent_(connectorID)
                ._affectedConnector_(connectorID)
                .build();
        return SERIALIZER.serialize(msg);
    }

    /**
     * Create a ConnectorUpdateMessage used for registering the connector at a broker
     *
     * @param securityToken the DAT Token used for this request
     * @param INFO_MODEL_VERSION the Infomodel Version of the connector
     * @param connectorID the ID of the connector
     * @return the {@link ConnectorUpdateMessage} as JSONLD
     * @throws IOException when the message cannot be serialized properly
     */
    public static String buildUpdateMessage(DynamicAttributeToken securityToken, String INFO_MODEL_VERSION, URI connectorID) throws IOException {
        var msg = new ConnectorUpdateMessageBuilder()
                ._securityToken_(securityToken)
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._modelVersion_(INFO_MODEL_VERSION)
                ._issuerConnector_(connectorID)
                ._senderAgent_(connectorID)
                ._affectedConnector_(connectorID)
                .build();
        return SERIALIZER.serialize(msg);
    }

    /**
     * Create a QueryMessage used for querying the broker
     *
     * @param securityToken the DAT Token used for this request
     * @param INFO_MODEL_VERSION the Infomodel Version of the connector
     * @param connectorID the ID of the connector
     * @param queryLanguage the Language of the Query (e.g. SPARQL, SQL, XQUERY)
     * @param queryScope the Scope of the Query (ALL connectors, ACTIVE connectors, INACTIVE connectors) {@link QueryScope}
     * @param queryTarget the type of IDS Components that are queried {@link QueryTarget}
     * @return the {@link QueryMessage} as JSONLD
     * @throws IOException when the message cannot be serialized properly
     */
    public static String buildQueryMessage(DynamicAttributeToken securityToken, String INFO_MODEL_VERSION, URI connectorID, QueryLanguage queryLanguage, QueryScope queryScope, QueryTarget queryTarget) throws IOException {
        var msg = new QueryMessageBuilder()
                ._securityToken_(securityToken)
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._modelVersion_(INFO_MODEL_VERSION)
                ._issuerConnector_(connectorID)
                ._senderAgent_(connectorID)
                ._queryLanguage_(queryLanguage)
                ._queryScope_(queryScope)
                ._recipientScope_(queryTarget)
                .build();
        return SERIALIZER.serialize(msg);
    }

    /**
     * Generates a two part multipart message. First part includes the message type (register, update, unavailable) and
     * second part contains the connector self declaration.
     *
     * @param header          String representation of the header message
     * @param selfDeclaration String representation of the connector self declaration
     * @return Two part multipart message containing the message header and self declaration as body
     */
    public static MultipartBody buildRequestBody(String header, String selfDeclaration) {
        var builder = new MultipartBody.Builder();
        builder.setType(MultipartBody.FORM);
        builder.addFormDataPart("header", header);
        builder.addFormDataPart("payload", selfDeclaration);
        return builder.build();
    }

}
