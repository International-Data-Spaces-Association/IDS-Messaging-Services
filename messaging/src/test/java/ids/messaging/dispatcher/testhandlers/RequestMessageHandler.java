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
package ids.messaging.dispatcher.testhandlers;

import java.net.URI;

import de.fraunhofer.iais.eis.RejectionReason;
import de.fraunhofer.iais.eis.RequestMessageImpl;
import ids.messaging.handler.message.MessageHandler;
import ids.messaging.handler.message.MessagePayload;
import ids.messaging.handler.message.SupportedMessageType;
import ids.messaging.response.ErrorResponse;
import ids.messaging.response.MessageResponse;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@SupportedMessageType(RequestMessageImpl.class)
public class RequestMessageHandler implements MessageHandler<RequestMessageImpl> {
    @Override
    public MessageResponse handleMessage(final RequestMessageImpl queryHeader,
                                         final MessagePayload payload) {
        return ErrorResponse.withDefaultHeader(RejectionReason.BAD_PARAMETERS,
                                               "request",
                                               URI.create("http://uri"),
                                               "4.0");
    }
}
