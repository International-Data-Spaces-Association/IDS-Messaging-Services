package de.fraunhofer.ids.framework.messaging.protocol.multipart.mapping;

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.Participant;
import de.fraunhofer.ids.framework.messaging.protocol.multipart.MessageAndPayload;
import de.fraunhofer.ids.framework.messaging.protocol.multipart.SerializedPayload;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.util.Optional;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class ParticipantNotificationMAP implements MessageAndPayload<Message, Participant> {
    final Message message;
    Participant participantSelfDescription;

    public ParticipantNotificationMAP(final Message message) {
        this.message = message;
    }

    public ParticipantNotificationMAP(final Message message, final Participant participantSelfDescription) {
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
        } else {
            return SerializedPayload.EMPTY;
        }
    }
}
