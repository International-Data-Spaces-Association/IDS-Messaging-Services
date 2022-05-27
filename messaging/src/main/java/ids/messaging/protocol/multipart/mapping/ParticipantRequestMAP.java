/*
 * Copyright Fraunhofer Institute for Software and Systems Engineering
 *
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
 *
 *  Contributors:
 *       sovity GmbH
 *
 */
package ids.messaging.protocol.multipart.mapping;

import java.util.Optional;

import de.fraunhofer.iais.eis.ParticipantRequestMessage;
import ids.messaging.protocol.multipart.MessageAndPayload;
import ids.messaging.protocol.multipart.SerializedPayload;

/**
 * MAP representing the ParticipantRequestMessage.
 */
public class ParticipantRequestMAP implements MessageAndPayload<ParticipantRequestMessage, Void> {

    /**
     * The ParticipantRequestMessage.
     */
    private ParticipantRequestMessage message;

    /**
     * Constructor for the ParticipantRequestMAP.
     *
     * @param message The ParticipantRequestMessage.
     */
    public ParticipantRequestMAP(final ParticipantRequestMessage message) {
        this.message = message;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ParticipantRequestMessage getMessage() {
        return message;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Void> getPayload() {
        return Optional.empty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SerializedPayload serializePayload() {
        return SerializedPayload.EMPTY;
    }
}
