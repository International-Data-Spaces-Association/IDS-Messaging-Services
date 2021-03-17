package de.fraunhofer.ids.framework.messaging.protocol.multipart;

import java.util.Optional;

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.MessageProcessedNotificationMessage;

public class MessageProcessedNotificationMAP implements MessageAndPayload<MessageProcessedNotificationMessage, Void> {

    private final MessageProcessedNotificationMessage message;

/*    public DefaultSuccessMAP( URI issuerConnector, String messageModelVersion, URI originalMessage, DynamicAttributeToken securityToken, URI senderAgent) {
    message = new MessageProcessedNotificationMessageBuilder()
            ._issuerConnector_(issuerConnector)
            ._issued_(CalendarUtil.now())
            ._modelVersion_(messageModelVersion)
            ._correlationMessage_(originalMessage)
            ._securityToken_(securityToken)
            ._senderAgent_(senderAgent)
            .build();
    }*/

    public MessageProcessedNotificationMAP( MessageProcessedNotificationMessage message ) {
        this.message = message;
    }

    @Override
    public MessageProcessedNotificationMessage getMessage() {
        return message;
    }

    @Override
    public Optional<Void> getPayload() {
        return Optional.empty();
    }

    @Override
    public SerializedPayload serializePayload() {
        return SerializedPayload.EMPTY;
    }
}

