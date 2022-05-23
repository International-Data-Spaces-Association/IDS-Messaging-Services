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
package ids.messaging.handler.request;

import java.util.Optional;

import de.fraunhofer.iais.eis.Message;
import ids.messaging.handler.message.MessageHandler;

/**
 * An instance of RequestHandler must find a MessageHandler for a given type of RequestMessage,
 * if a handler exists.
 */
public interface RequestMessageHandler {

    /**
     * Find the right {@link MessageHandler} for the given MessageType.
     *
     * @param messageType class of the RequestMessage subtype a handler should be found for
     * @param <R>         some subtype of RequestMessage
     * @return a MessageHandler for the given messageType or Optional.Empty if no Handler exists
     */
    <R extends Message> Optional<MessageHandler<R>> resolveHandler(Class<R> messageType);
}
