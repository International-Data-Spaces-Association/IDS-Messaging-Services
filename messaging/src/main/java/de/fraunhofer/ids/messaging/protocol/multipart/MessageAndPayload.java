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
package de.fraunhofer.ids.messaging.protocol.multipart;

import java.util.Optional;

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.ids.messaging.common.SerializeException;

public interface MessageAndPayload<M extends Message, T> {

    /**
     * Get the message of the received response.
     * @return The message.
     */
    M getMessage();

    /**
     * Get the payload of the received response.
     * @return The payload.
     */
    Optional<T> getPayload();

    /**
     * @throws SerializeException exception  is thrown if serializing a message threw an IOException
     * @return The serzialized payload.
     */
    SerializedPayload serializePayload() throws SerializeException;
}
