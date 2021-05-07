package de.fraunhofer.ids.framework.appstore;

import java.net.URI;

import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.util.ConstraintViolationException;
import de.fraunhofer.iais.eis.util.Util;
import de.fraunhofer.ids.framework.messaging.util.IdsMessageUtils;
import de.fraunhofer.iais.eis.util.ConstraintViolationException;

/**
 * The MessageUtils class contains utility methods for building Infomodel Messages (used by the {@link AppStoreService} class)
 */
public class MessageBuilder {

    /**
     * Utility classes (only static methods and fields) do not have a public constructor.
     * Instantiating them does not make sense, prevent instantiating.
     */
    protected MessageBuilder() {
        throw new UnsupportedOperationException();
    }

    /**
     * Create a SelfDescriptionMessage used for receiving metadata from an IDS Infrastructure Component
     *
     * @param securityToken the DAT Token used for this request (optional)
     * @param connector     the used connector
     *
     * @return the {@link ConnectorUpdateMessage}
     *
     * @throws ConstraintViolationException when the message cannot be serialized properly
     */
    public static DescriptionRequestMessage buildDescriptionRequestMessage( final DynamicAttributeToken securityToken,
                                                                            final Connector connector ) {
        var builder = new DescriptionRequestMessageBuilder()
                ._modelVersion_(connector.getOutboundModelVersion())
                ._issuerConnector_(connector.getId())
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._senderAgent_(connector.getId());

        if( securityToken != null ) {
            builder._securityToken_(securityToken);
        }
        return builder.build();

    }

    /**
     * Create a AppDescriptionMessage used for receiving metadata from an IDS Infrastructure Component
     *
     * @param securityToken the DAT Token used for this request
     * @param connector     the used connector
     * @param app the app that metadata is required from
     *
     * @return the {@link ConnectorUpdateMessage}
     *
     * @throws ConstraintViolationException when the message cannot be serialized properly
     */
    public static DescriptionRequestMessage buildAppDescriptionRequestMessage( final DynamicAttributeToken securityToken,
                                                                            final Connector connector, final URI app ) {
        return new DescriptionRequestMessageBuilder()
                ._modelVersion_(connector.getOutboundModelVersion())
                ._issuerConnector_(connector.getId())
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._senderAgent_(connector.getId())
                ._requestedElement_(app)
                ._securityToken_(securityToken)
                .build();
    }
    /**
     * Create a AppDescriptionMessage used for receiving metadata from an IDS Infrastructure Component
     *
     * @param securityToken the DAT Token used for this request
     * @param connector     the used connector
     * @param app the app that metadata is required from
     *
     * @return the {@link ConnectorUpdateMessage}
     *
     * @throws ConstraintViolationException when the message cannot be serialized properly
     */
    public static ArtifactRequestMessage buildAppArtifactRequestMessage( final DynamicAttributeToken securityToken,
                                                                               final Connector connector, final URI app ) {
        return new ArtifactRequestMessageBuilder()
                ._modelVersion_(connector.getOutboundModelVersion())
                ._issuerConnector_(connector.getId())
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._senderAgent_(connector.getId())
                ._requestedArtifact_(app)
                ._securityToken_(securityToken)
                .build();
    }
}
