package de.fraunhofer.ids.framework.messaging.handler.message;

import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Implementation of {@link MessagePayload} interface. Can parse payload from JSON and return the resulting inputstream.
 */
public class MessagePayloadInputstream implements MessagePayload {

    private final InputStream  underlyingInputStream;
    private final ObjectMapper objectMapper;

    public MessagePayloadInputstream( final InputStream underlyingInputStream, final ObjectMapper objectMapper ) {
        this.underlyingInputStream = underlyingInputStream;
        this.objectMapper = objectMapper;
    }

    @Override
    public InputStream getUnderlyingInputStream() {
        return underlyingInputStream;
    }

    @Override
    public <T> T readFromJSON( final Class<? extends T> targetType ) throws IOException {
        return this.objectMapper.readValue(underlyingInputStream, targetType);
    }
}
