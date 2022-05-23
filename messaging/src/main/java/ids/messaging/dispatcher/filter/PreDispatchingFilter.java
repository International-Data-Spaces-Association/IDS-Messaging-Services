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
package ids.messaging.dispatcher.filter;

import de.fraunhofer.iais.eis.Message;

/**
 * A filter that can be used for processing the message before it gets to the
 * (can be used to filter and reject some messages after
 * custom criteria before they get to the MessageHandler).
 */
@FunctionalInterface
public interface PreDispatchingFilter {
    /**
     * Execute the PreDispatchingFilter and return the result.
     *
     * @param in The RequestMessage to be filtered.
     * @return Result of the processing.
     * @throws PreDispatchingFilterException If an error occurs while processing the message.
     */
    PreDispatchingFilterResult process(Message in) throws PreDispatchingFilterException;
}
