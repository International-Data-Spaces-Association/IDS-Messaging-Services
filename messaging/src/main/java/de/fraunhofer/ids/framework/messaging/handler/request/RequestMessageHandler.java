package de.fraunhofer.ids.framework.messaging.handler.request;

import java.util.Optional;

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.RequestMessage;
import de.fraunhofer.ids.framework.messaging.handler.message.MessageHandler;

/**
 * An instance of RequestHandler must find a {@link MessageHandler} for a given type of {@link RequestMessage},
 * if a handler exists.
 */
public interface RequestMessageHandler {

    /**
     * Find the right {@link MessageHandler} for the given MessageType
     *
     * @param messageType class of the RequestMessage subtype a handler should be found for
     * @param <R>         some subtype of RequestMessage
     *
     * @return a MessageHandler for the given messageType or Optional.Empty if no Handler exists
     */
    <R extends Message> Optional<MessageHandler<R>> resolveHandler( Class<R> messageType );
}
