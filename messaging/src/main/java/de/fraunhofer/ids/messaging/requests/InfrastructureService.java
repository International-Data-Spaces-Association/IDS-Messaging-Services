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
package de.fraunhofer.ids.messaging.requests;

import de.fraunhofer.iais.eis.RejectionMessage;
import de.fraunhofer.ids.messaging.core.config.ConfigContainer;
import de.fraunhofer.ids.messaging.core.daps.DapsTokenProvider;
import de.fraunhofer.ids.messaging.protocol.MessageService;
import de.fraunhofer.ids.messaging.protocol.UnexpectedResponseException;
import de.fraunhofer.ids.messaging.protocol.multipart.mapping.RejectionMAP;
import de.fraunhofer.ids.messaging.protocol.multipart.MessageAndPayload;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PROTECTED)
public abstract class InfrastructureService  {
    ConfigContainer   container;
    DapsTokenProvider tokenProvider;
    MessageService messageService;

    /**
     * Check if incoming response if of expected type, throw an IOException with information, if it is not.
     *
     * @param response {@link MessageAndPayload} as returned by the {@link MessageService}
     * @param expectedType Expected type response MAP should have
     * @param <T> expected Type as generic
     * @return {@link MessageAndPayload object specialized to the expected Message}
     * @throws UnexpectedResponseException if a rejection message or any other unexpected message was returned.
     */
    protected <T extends MessageAndPayload<?, ?>> T expectMapOfTypeT(
            final MessageAndPayload<?, ?> response,
            final Class<T> expectedType) throws UnexpectedResponseException {

        if (expectedType.isAssignableFrom(response.getClass())) {
            return expectedType.cast(response);
        }

        if (response instanceof RejectionMAP) {
            final var rejectionMessage = (RejectionMessage) response.getMessage();
            throw new UnexpectedResponseException(
                    String.format(
                            "Message rejected by target with following Reason: %s",
                            rejectionMessage.getRejectionReason()
                    )
            );
        }
        throw new UnexpectedResponseException(
                String.format(
                        "Unexpected Message of type %s was returned, expected Message of type %s",
                        response.getMessage().getClass().toString(), expectedType.getSimpleName()
                )
        );
    }

}
