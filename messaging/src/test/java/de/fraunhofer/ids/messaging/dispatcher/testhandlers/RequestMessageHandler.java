package de.fraunhofer.ids.messaging.dispatcher.testhandlers;

import java.net.URI;

import de.fraunhofer.iais.eis.RejectionReason;
import de.fraunhofer.iais.eis.RequestMessageImpl;
import de.fraunhofer.ids.messaging.handler.message.MessageHandler;
import de.fraunhofer.ids.messaging.handler.message.MessageHandlerException;
import de.fraunhofer.ids.messaging.handler.message.MessagePayload;
import de.fraunhofer.ids.messaging.handler.message.SupportedMessageType;
import de.fraunhofer.ids.messaging.response.ErrorResponse;
import de.fraunhofer.ids.messaging.response.MessageResponse;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@SupportedMessageType(RequestMessageImpl.class)
public class RequestMessageHandler implements MessageHandler<RequestMessageImpl> {
    @Override
    public MessageResponse handleMessage( final RequestMessageImpl queryHeader,
                                          final MessagePayload payload) throws MessageHandlerException {
        return ErrorResponse.withDefaultHeader(RejectionReason.BAD_PARAMETERS,
                                               "request",
                                               URI.create("http://uri"),
                                               "4.0");
    }
}
