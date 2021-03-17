package de.fraunhofer.ids.framework.messaging.protocol.multipart;

import de.fraunhofer.iais.eis.ParticipantRequestMessage;

import java.util.Optional;

@Deprecated
public class ParticipantRequestMAP implements MessageAndPayload<ParticipantRequestMessage, Void> {
    private ParticipantRequestMessage message;
    public ParticipantRequestMAP(ParticipantRequestMessage message) {
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
