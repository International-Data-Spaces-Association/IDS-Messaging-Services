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
package ids.messaging.requests.exceptions;

import ids.messaging.requests.MessageContainer;
import lombok.Getter;

/**
 * Exception which is thrown if payload is unexpected.
 */
@Getter
public class UnexpectedPayloadException extends IdsRequestException {

    /**
     * The MessageContainer.
     */
    private final MessageContainer<?> messageContainer;

    /**
     * Constructor for the UnexpectedPayloadException.
     *
     * @param messageContainer The MessageContainer.
     */
    public UnexpectedPayloadException(final MessageContainer<?> messageContainer) {
        super();
        this.messageContainer = messageContainer;
    }

    /**
     * Constructor for the UnexpectedPayloadException.
     *
     * @param message The exception message.
     * @param messageContainer The MessageContainer.
     */
    public UnexpectedPayloadException(final String message,
                                      final MessageContainer<?> messageContainer) {
        super(message);
        this.messageContainer = messageContainer;
    }
}
