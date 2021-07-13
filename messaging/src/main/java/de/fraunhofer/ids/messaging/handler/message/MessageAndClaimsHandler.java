package de.fraunhofer.ids.messaging.handler.message;

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.ids.messaging.response.MessageResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;

import java.util.Optional;

/**
 * MessageHandler, also passing DAT Claims for additional checks
 *
 * @param <T> Type of Message accepted by handler
 */
public interface MessageAndClaimsHandler<T extends Message> extends MessageHandler<T>{

    /**
     * {@inheritDoc}
     */
    @Override
    default MessageResponse handleMessage(T queryHeader, MessagePayload payload) throws MessageHandlerException {
        return handleMessage(queryHeader, payload, Optional.empty());
    }

    /**
     * @param queryHeader IDS Message Header
     * @param payload Payload of Message
     * @param optionalClaimsJws optional containing claims of the messages DAT
     * @return Response (which will be sent back to the requesting connector)
     * @throws MessageHandlerException when some error happens while handling the message
     */
    MessageResponse handleMessage(T queryHeader, MessagePayload payload, Optional<Jws<Claims>> optionalClaimsJws) throws MessageHandlerException;


}
