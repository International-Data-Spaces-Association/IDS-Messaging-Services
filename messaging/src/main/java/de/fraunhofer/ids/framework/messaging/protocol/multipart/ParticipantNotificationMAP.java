package de.fraunhofer.ids.framework.messaging.protocol.multipart;

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.Participant;

import java.util.Optional;

public class ParticipantNotificationMAP implements MessageAndPayload<Message, Participant> {
    private final Message message;
    private Participant participantSelfDescription;
    public ParticipantNotificationMAP(Message message) {
        this.message = message;
    }

    public ParticipantNotificationMAP(Message message, Participant participantSelfDescription)
    {
        this.message = message;
        this.participantSelfDescription = participantSelfDescription;
    }
    @Override
    public Message getMessage() {
        return message;
    }

    @Override
    public Optional<Participant> getPayload() {
        return Optional.of(participantSelfDescription);
    }

    @Override
    public SerializedPayload serializePayload() {
        if (participantSelfDescription != null) {
            return new SerializedPayload(participantSelfDescription.toRdf().getBytes(), "application/ld+json");
        }
        else return SerializedPayload.EMPTY;
    }
}