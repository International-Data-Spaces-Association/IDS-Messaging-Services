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
package ids.messaging.handler.message;

import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.databind.ObjectMapper;
import ids.messaging.common.DeserializeException;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Implementation of {@link MessagePayload} interface.
 * Can parse payload from JSON and return the resulting inputstream.
 */
@AllArgsConstructor
public class MessagePayloadInputstream implements MessagePayload {
    /**
     * The complete raw InputStream.
     */
    @Getter
    private final InputStream underlyingInputStream;

    /**
     * The ObjectMapper.
     */
    private final ObjectMapper objectMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T readFromJSON(final Class<? extends T> targetType)
            throws DeserializeException {
        try {
            return this.objectMapper
                    .readValue(underlyingInputStream, targetType);
        } catch (IOException ioException) {
            throw new DeserializeException(ioException);
        }
    }
}
