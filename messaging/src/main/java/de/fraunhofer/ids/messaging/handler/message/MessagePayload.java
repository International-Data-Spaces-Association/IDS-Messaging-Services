package de.fraunhofer.ids.messaging.handler.message;

import java.io.IOException;
import java.io.InputStream;

/**
 * Wrapper for payloads of incoming Messages. Implementation can be found in {@link MessagePayloadInputstream}.
 */
public interface MessagePayload {
    /**
     * Getter for the InputStream of the incoming message.
     *
     * @return get the InputSteam of the incoming message
     */
    InputStream getUnderlyingInputStream();

    /**
     * @param targetType type that should be parsed from the message
     * @param <T>        type of the parsed object
     * @return underlying input stream parsed as targetType
     * @throws IOException if underlying input stream cannot be parsed
     */
    <T> T readFromJSON(Class<? extends T> targetType) throws IOException;
}
