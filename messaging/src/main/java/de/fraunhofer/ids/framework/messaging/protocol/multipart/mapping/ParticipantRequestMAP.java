package de.fraunhofer.ids.framework.messaging.protocol.multipart.mapping;

import de.fraunhofer.iais.eis.ParticipantRequestMessage;
import de.fraunhofer.ids.framework.messaging.protocol.multipart.MessageAndPayload;
import de.fraunhofer.ids.framework.messaging.protocol.multipart.SerializedPayload;

import java.util.Optional;

public class ParticipantRequestMAP implements MessageAndPayload<ParticipantRequestMessage, Void> {
    private ParticipantRequestMessage message;
    public ParticipantRequestMAP(final ParticipantRequestMessage message) {
        this.message = message;
    }

    @Override
    public ParticipantRequestMessage getMessage() {
        return message;
    }

    @Override
    public Optional<Void> getPayload() {
        return Optional.empty();
    }

    @Override
    public SerializedPayload serializePayload() {
        return SerializedPayload.EMPTY;
    }
}
