package de.fraunhofer.ids.framework.messaging.handler.message;

import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

/**
 * Implementation of {@link MessagePayload} interface. Can parse payload from JSON and return the resulting inputstream.
 */
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class MessagePayloadInputstream implements MessagePayload {
    @Getter
    InputStream  underlyingInputStream;

    ObjectMapper objectMapper;

    @Override
    public <T> T readFromJSON(final Class<? extends T> targetType) throws IOException {
        return this.objectMapper.readValue(underlyingInputStream, targetType);
    }
}
