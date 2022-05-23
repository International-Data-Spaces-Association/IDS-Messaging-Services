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

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import ids.messaging.common.SerializeException;
import ids.messaging.protocol.multipart.MessageAndPayload;
import ids.messaging.protocol.multipart.SerializedPayload;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

/**
 * MAP representing a generic message.
 */
@AllArgsConstructor
@RequiredArgsConstructor
public class GenericMessageAndPayload implements MessageAndPayload<Message, Object> {
    /**
     * The message.
     */
    @Getter
    @NotNull
    private Message message;

    /**
     * The payload.
     */
    private Object payload;

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Object> getPayload() {
        return Optional.ofNullable(payload);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SerializedPayload serializePayload() throws SerializeException {
        SerializedPayload serializedPayload;
        if (Objects.nonNull(payload)) {
            try {
                serializedPayload = new SerializedPayload(
                        new Serializer().serialize(payload).getBytes(),
                        "application/ld+json");
            } catch (IOException ioException) {
                throw new SerializeException(ioException);
            }
        } else {
            serializedPayload =  SerializedPayload.EMPTY;
        }
        return serializedPayload;
    }
}
