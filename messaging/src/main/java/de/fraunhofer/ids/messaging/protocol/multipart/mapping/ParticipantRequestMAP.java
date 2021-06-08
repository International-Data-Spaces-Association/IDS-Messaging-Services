package de.fraunhofer.ids.messaging.protocol.multipart.mapping;

import java.util.Optional;

import de.fraunhofer.iais.eis.ParticipantRequestMessage;
import de.fraunhofer.ids.messaging.protocol.multipart.MessageAndPayload;
import de.fraunhofer.ids.messaging.protocol.multipart.SerializedPayload;

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
