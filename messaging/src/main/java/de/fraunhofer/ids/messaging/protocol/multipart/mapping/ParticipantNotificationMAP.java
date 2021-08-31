/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.fraunhofer.ids.messaging.protocol.multipart.mapping;

import java.util.Optional;

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.Participant;
import de.fraunhofer.ids.messaging.protocol.multipart.MessageAndPayload;
import de.fraunhofer.ids.messaging.protocol.multipart.SerializedPayload;

/**
 * MAP representing the ParticipantNotificationMAP.
 */
public class ParticipantNotificationMAP
        implements MessageAndPayload<Message, Participant> {

    /**
     * The message.
     */
    private final Message message;

    /**
     * The participant self description.
     */
    private Participant participantSelfDescription;

    /**
     * Constructor for the ParticipantNotificationMAP.
     * @param message The message.
     */
    public ParticipantNotificationMAP(final Message message) {
        this.message = message;
    }

    /**
     * Constructor for ParticipantNotificationMAP.
     *
     * @param message The message.
     * @param participantSelfDescription The self description.
     */
    public ParticipantNotificationMAP(
            final Message message,
            final Participant participantSelfDescription) {
        this.message = message;
        this.participantSelfDescription = participantSelfDescription;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Message getMessage() {
        return message;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Participant> getPayload() {
        return Optional.of(participantSelfDescription);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SerializedPayload serializePayload() {
        if (participantSelfDescription != null) {
            return new SerializedPayload(
                    participantSelfDescription.toRdf().getBytes(),
                    "application/ld+json");
        } else {
            return SerializedPayload.EMPTY;
        }
    }
}
