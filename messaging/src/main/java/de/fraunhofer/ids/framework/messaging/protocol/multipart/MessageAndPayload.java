package de.fraunhofer.ids.framework.messaging.protocol.multipart;


import de.fraunhofer.iais.eis.Message;

import java.io.IOException;
import java.util.Optional;

public interface MessageAndPayload<MessageType extends Message, T> {

    MessageType getMessage();
    Optional<T> getPayload();
    SerializedPayload serializePayload() throws IOException;


}

