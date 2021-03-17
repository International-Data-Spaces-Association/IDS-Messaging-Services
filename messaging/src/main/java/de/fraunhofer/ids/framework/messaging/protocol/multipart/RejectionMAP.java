package de.fraunhofer.ids.framework.messaging.protocol.multipart;

import java.util.Optional;

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.RejectionMessage;

public class RejectionMAP implements MessageAndPayload<RejectionMessage, String> {
    private RejectionMessage message;
    private String payload;
    public RejectionMAP( RejectionMessage message, String payloadString ) {

    }

    @Override
    public RejectionMessage getMessage() {
        return message;
    }

    @Override
    public Optional<String> getPayload() {
        return Optional.of(payload);
    }

    @Override
    public SerializedPayload serializePayload() {
        return new SerializedPayload(payload.getBytes(), "text/plain");
    }
}
