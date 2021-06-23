package de.fraunhofer.ids.messaging.paris;

import java.net.URI;

import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.ids.component.core.util.CalendarUtil;
import lombok.experimental.UtilityClass;

@UtilityClass
public class MessageBuilder {
    public static ParticipantUpdateMessage buildParticipantUpdateMessage( final DynamicAttributeToken securityToken, final Connector connector ) {
        return new ParticipantUpdateMessageBuilder()
                ._issued_(CalendarUtil.now())
                ._modelVersion_(connector.getOutboundModelVersion())
                ._issuerConnector_(connector.getId())
                ._securityToken_(securityToken)
                ._affectedParticipant_(connector.getId())
                ._senderAgent_(connector.getId())
                .build();
    }

    public static ParticipantRequestMessage buildParticipantRequestMessage( final DynamicAttributeToken securityToken, final Connector connector, final URI participant) {
        return new ParticipantRequestMessageBuilder()
                ._issued_(CalendarUtil.now())
                ._modelVersion_(connector.getOutboundModelVersion())
                ._issuerConnector_(connector.getId())
                ._securityToken_(securityToken)
                ._requestedParticipant_(participant)
                ._senderAgent_(connector.getId())
                .build();
    }

    public static ParticipantUnavailableMessage buildParticipantUnavailableMessage( final DynamicAttributeToken securityToken, final Connector connector, final URI participant) {
        return new ParticipantUnavailableMessageBuilder()
                ._issued_(CalendarUtil.now())
                ._modelVersion_(connector.getOutboundModelVersion())
                ._issuerConnector_(connector.getId())
                ._securityToken_(securityToken)
                ._affectedParticipant_(participant)
                ._senderAgent_(connector.getId())
                .build();
    }


}
