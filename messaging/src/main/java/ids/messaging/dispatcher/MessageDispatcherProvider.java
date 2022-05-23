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
package ids.messaging.dispatcher;

import com.fasterxml.jackson.databind.ObjectMapper;
import ids.messaging.core.config.ConfigContainer;
import ids.messaging.core.daps.DapsValidator;
import ids.messaging.handler.request.RequestMessageHandler;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * Providing a MessageDispatcher as a bean, for autowiring.
 */
@Slf4j
@Component
@NoArgsConstructor
public class MessageDispatcherProvider {
    /**
     * Make use of autowiring to get the parameters for the MessageDispatchers constructor and
     * create an Instance of MessageDispatcher with them.
     *
     * @param objectMapper For parsing objects from json.
     * @param configContainer Container for current configuration.
     * @param resolver Resolver for finding the right handler for infomodel
     * {@link de.fraunhofer.iais.eis.Message}.
     * @param dapsValidator Verification of DAT tokens.
     * @return MessageDispatcher as Spring Bean.
     */
    @Bean
    public MessageDispatcher provideMessageDispatcher(
            final ObjectMapper objectMapper,
            final RequestMessageHandler resolver,
            final ConfigContainer configContainer,
            final DapsValidator dapsValidator) {

        return new MessageDispatcher(objectMapper,
                                     resolver,
                                     configContainer,
                                     dapsValidator);
    }
}
