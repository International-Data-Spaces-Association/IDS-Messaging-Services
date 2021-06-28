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
package de.fraunhofer.ids.messaging.handler.message;

import java.io.InputStream;

import de.fraunhofer.ids.messaging.common.DeserializeException;

/**
 * Wrapper for payloads of incoming Messages. Implementation can be found in {@link MessagePayloadInputstream}.
 */
public interface MessagePayload {
    /**
     * Getter for the InputStream of the incoming message.
     *
     * @return get the InputSteam of the incoming message
     */
    InputStream getUnderlyingInputStream();

    /**
     * @param targetType type that should be parsed from the message
     * @param <T>        type of the parsed object
     * @return underlying input stream parsed as targetType
     * @throws DeserializeException if underlying input stream cannot be parsed
     */
    <T> T readFromJSON(Class<? extends T> targetType)
            throws DeserializeException;
}
