package de.fraunhofer.ids.framework.messaging.protocol.multipart;


import de.fraunhofer.iais.eis.Message;

import java.io.IOException;
import java.util.Optional;

public interface MessageAndPayload<M extends Message, T> {

    M getMessage();
    Optional<T> getPayload();
    SerializedPayload serializePayload() throws IOException;


}

