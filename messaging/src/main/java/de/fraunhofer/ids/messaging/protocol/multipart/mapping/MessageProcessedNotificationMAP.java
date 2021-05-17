package de.fraunhofer.ids.messaging.protocol.multipart.mapping;

import java.util.Optional;

import de.fraunhofer.iais.eis.MessageProcessedNotificationMessage;
import de.fraunhofer.ids.messaging.protocol.multipart.MessageAndPayload;
import de.fraunhofer.ids.messaging.protocol.multipart.SerializedPayload;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class MessageProcessedNotificationMAP implements MessageAndPayload<MessageProcessedNotificationMessage, Void> {

    MessageProcessedNotificationMessage message;

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

