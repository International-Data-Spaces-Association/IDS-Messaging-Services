package de.fraunhofer.ids.framework.messaging.dispatcher.testhandlers;

import de.fraunhofer.iais.eis.NotificationMessage;
import de.fraunhofer.iais.eis.NotificationMessageImpl;
import de.fraunhofer.iais.eis.RejectionReason;
import de.fraunhofer.ids.framework.messaging.handler.message.MessageHandler;
import de.fraunhofer.ids.framework.messaging.handler.message.MessageHandlerException;
import de.fraunhofer.ids.framework.messaging.handler.message.MessagePayload;
import de.fraunhofer.ids.framework.messaging.handler.message.SupportedMessageType;
import de.fraunhofer.ids.framework.messaging.response.ErrorResponse;
import de.fraunhofer.ids.framework.messaging.response.MessageResponse;
import lombok.NoArgsConstructor;

import java.net.URI;

@NoArgsConstructor
@SupportedMessageType(NotificationMessageImpl.class)
public class NotificationMessageHandler implements MessageHandler<NotificationMessageImpl> {
    @Override
    public MessageResponse handleMessage(NotificationMessageImpl queryHeader, MessagePayload payload) throws MessageHandlerException {
        return ErrorResponse.withDefaultHeader(RejectionReason.BAD_PARAMETERS, "notification", URI.create("http://uri"),"4.0");
    }
}
