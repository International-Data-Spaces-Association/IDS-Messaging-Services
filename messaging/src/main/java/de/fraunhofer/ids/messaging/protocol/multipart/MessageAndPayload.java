package de.fraunhofer.ids.messaging.protocol.multipart;

import java.io.IOException;
import java.util.Optional;

import de.fraunhofer.iais.eis.Message;

public interface MessageAndPayload<M extends Message, T> {

    M getMessage();
    Optional<T> getPayload();
    SerializedPayload serializePayload() throws IOException;
}
