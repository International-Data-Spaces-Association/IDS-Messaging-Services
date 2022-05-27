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

import java.util.Optional;

import de.fraunhofer.iais.eis.Message;
import ids.messaging.response.MessageResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;

/**
 * MessageHandler, also passing DAT Claims for additional checks.
 *
 * @param <T> Type of Message accepted by handler
 */
public interface MessageAndClaimsHandler<T extends Message> extends MessageHandler<T> {

    /**
     * {@inheritDoc}
     */
    @Override
    default MessageResponse handleMessage(T queryHeader, MessagePayload payload)
            throws MessageHandlerException {
        return handleMessage(queryHeader, payload, Optional.empty());
    }

    /**
     * @param queryHeader IDS Message Header.
     * @param payload Payload of Message.
     * @param optionalClaimsJws Optional containing claims of the messages DAT.
     * @return Response (which will be sent back to the requesting connector).
     * @throws MessageHandlerException When some error happens while handling the message.
     */
    MessageResponse handleMessage(T queryHeader, MessagePayload payload,
                                  Optional<Jws<Claims>> optionalClaimsJws)
            throws MessageHandlerException;


}
