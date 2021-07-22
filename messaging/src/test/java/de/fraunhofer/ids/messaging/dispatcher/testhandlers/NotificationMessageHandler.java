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
package de.fraunhofer.ids.messaging.dispatcher.testhandlers;

import de.fraunhofer.iais.eis.NotificationMessageImpl;
import de.fraunhofer.ids.messaging.handler.message.*;
import de.fraunhofer.ids.messaging.response.MessageResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import lombok.NoArgsConstructor;

import java.util.Optional;

@NoArgsConstructor
@SupportedMessageType(NotificationMessageImpl.class)
public class NotificationMessageHandler implements MessageAndClaimsHandler<NotificationMessageImpl> {
    @Override
    public MessageResponse handleMessage(final NotificationMessageImpl queryHeader,
                                         final MessagePayload payload, final Optional<Jws<Claims>> claimsJws) throws MessageHandlerException {
        throw new MessageHandlerException("Failed to handle message!");
    }
}
