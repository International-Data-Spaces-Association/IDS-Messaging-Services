package de.fraunhofer.ids.framework.messaging.protocol.multipart.mapping;

import java.util.Optional;

import de.fraunhofer.iais.eis.MessageProcessedNotificationMessage;
import de.fraunhofer.ids.framework.messaging.protocol.multipart.MessageAndPayload;
import de.fraunhofer.ids.framework.messaging.protocol.multipart.SerializedPayload;

public class MessageProcessedNotificationMAP implements MessageAndPayload<MessageProcessedNotificationMessage, Void> {

    private final MessageProcessedNotificationMessage message;

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

