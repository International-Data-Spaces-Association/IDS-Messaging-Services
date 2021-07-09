package de.fraunhofer.ids.messaging.handler.message;

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.ids.messaging.response.MessageResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;

import java.util.Optional;

public interface MessageAndClaimsHandler<T extends Message> extends MessageHandler<T>{

    @Override
    default MessageResponse handleMessage(T queryHeader, MessagePayload payload) throws MessageHandlerException {
        return handleMessage(queryHeader, payload, Optional.empty());
    }

    MessageResponse handleMessage(T queryHeader, MessagePayload payload, Optional<Jws<Claims>> optionalClaimsJws) throws MessageHandlerException;


}
