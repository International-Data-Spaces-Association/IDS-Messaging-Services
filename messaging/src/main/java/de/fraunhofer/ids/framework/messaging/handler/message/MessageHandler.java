package de.fraunhofer.ids.framework.messaging.handler.message;

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.ids.framework.messaging.response.MessageResponse;

public interface MessageHandler<T extends Message> {

    /**
     * Handle an incoming Message of type T and return a MessageResponse.
     *
     * @param queryHeader header part of the incoming Message (an instance of RequestMessage)
     * @param payload     payload of the Message (as MessagePayload, access with getUnderlyingInputStream())
     * @return an instance of MessageResponse (BodyResponse, ErrorResponse,...)
     * @throws MessageHandlerException if an error occurs while handling the incoming message
     */
    MessageResponse handleMessage(T queryHeader, MessagePayload payload) throws MessageHandlerException;
}
