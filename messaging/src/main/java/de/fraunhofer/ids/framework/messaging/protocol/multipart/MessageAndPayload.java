package de.fraunhofer.ids.framework.messaging.protocol.multipart;

import java.util.Optional;

import de.fraunhofer.iais.eis.Message;
import lombok.Getter;

public interface MessageAndPayload<MessageType extends Message, PayloadType> {


    MessageType getMessage();
    Optional<PayloadType> getPayload();
    SerializedPayload serializePayload();
}

