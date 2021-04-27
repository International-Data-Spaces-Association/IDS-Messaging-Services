package de.fraunhofer.ids.framework.messaging.dispatcher.testhandlers;

import de.fraunhofer.iais.eis.RejectionReason;
import de.fraunhofer.iais.eis.RequestMessageImpl;
import de.fraunhofer.ids.framework.messaging.handler.message.MessageHandler;
import de.fraunhofer.ids.framework.messaging.handler.message.MessageHandlerException;
import de.fraunhofer.ids.framework.messaging.handler.message.MessagePayload;
import de.fraunhofer.ids.framework.messaging.handler.message.SupportedMessageType;
import de.fraunhofer.ids.framework.messaging.response.ErrorResponse;
import de.fraunhofer.ids.framework.messaging.response.MessageResponse;
import lombok.NoArgsConstructor;

import java.net.URI;

@NoArgsConstructor
@SupportedMessageType(RequestMessageImpl.class)
public class RequestMessageHandler implements MessageHandler<RequestMessageImpl> {
    @Override
    public MessageResponse handleMessage(final RequestMessageImpl queryHeader,
                                         final MessagePayload payload) throws MessageHandlerException {
        return ErrorResponse.withDefaultHeader(RejectionReason.BAD_PARAMETERS,
                                               "request",
                                               URI.create("http://uri"),
                                               "4.0");
    }
}
