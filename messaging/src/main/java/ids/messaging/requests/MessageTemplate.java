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
package ids.messaging.requests;

import de.fraunhofer.iais.eis.Message;
import ids.messaging.core.daps.DapsTokenManagerException;

/**
 * Interface for building messages defined by the template.
 *
 * @param <T> The message type.
 */
@FunctionalInterface
public interface MessageTemplate<T extends Message> {

    /**
     * Build the message defined by this template.
     *
     * @return Built message by template.
     * @throws DapsTokenManagerException when no DAT for Message can be received.
     */
    T buildMessage() throws DapsTokenManagerException;
}
