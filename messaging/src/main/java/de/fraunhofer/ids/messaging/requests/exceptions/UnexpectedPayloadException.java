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
package de.fraunhofer.ids.messaging.requests.exceptions;

import de.fraunhofer.ids.messaging.requests.MessageContainer;
import lombok.Getter;

@Getter
public class UnexpectedPayloadException extends IdsRequestException{

    private final MessageContainer<?> messageContainer;

    public UnexpectedPayloadException(MessageContainer<?> messageContainer){super();
        this.messageContainer = messageContainer;
    }

    public UnexpectedPayloadException(String message, MessageContainer<?> messageContainer){super(message);
        this.messageContainer = messageContainer;
    }
}
