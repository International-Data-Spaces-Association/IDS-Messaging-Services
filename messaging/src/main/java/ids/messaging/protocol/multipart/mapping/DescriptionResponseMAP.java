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

import de.fraunhofer.iais.eis.DescriptionResponseMessage;
import ids.messaging.protocol.multipart.MessageAndPayload;
import ids.messaging.protocol.multipart.SerializedPayload;
import lombok.AllArgsConstructor;

/**
 * MAP representing the DescriptionResponseMessage.
 */
@AllArgsConstructor
public class DescriptionResponseMAP
        implements MessageAndPayload<DescriptionResponseMessage, String> {

    /**
     * The message.
     */
    private DescriptionResponseMessage message;

    /**
     * The payload.
     */
    private String payload;

    /**
     * {@inheritDoc}
     */
    @Override
    public DescriptionResponseMessage getMessage() {
        return message;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<String> getPayload() {
        return Optional.of(payload);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SerializedPayload serializePayload() {
        return new SerializedPayload(payload.getBytes(), "application/ld+json");
    }
}
