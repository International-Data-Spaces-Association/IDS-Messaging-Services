package de.fraunhofer.ids.messaging.dispatcher.testhandlers;

import java.net.URI;

import de.fraunhofer.iais.eis.NotificationMessageImpl;
import de.fraunhofer.iais.eis.RejectionReason;
import de.fraunhofer.ids.messaging.handler.message.MessageHandler;
import de.fraunhofer.ids.messaging.handler.message.MessageHandlerException;
import de.fraunhofer.ids.messaging.handler.message.MessagePayload;
import de.fraunhofer.ids.messaging.handler.message.SupportedMessageType;
import de.fraunhofer.ids.messaging.response.ErrorResponse;
import de.fraunhofer.ids.messaging.response.MessageResponse;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@SupportedMessageType(NotificationMessageImpl.class)
public class NotificationMessageHandler implements MessageHandler<NotificationMessageImpl> {
    @Override
    public MessageResponse handleMessage( final NotificationMessageImpl queryHeader,
                                          final MessagePayload payload) throws MessageHandlerException {
        throw new MessageHandlerException("Failed to handle message!");
    }
}
