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
package de.fraunhofer.ids.messaging.dispatcher;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.ids.messaging.core.config.ConfigContainer;
import de.fraunhofer.ids.messaging.core.daps.DapsPublicKeyProvider;
import de.fraunhofer.ids.messaging.core.daps.DapsValidator;
import de.fraunhofer.ids.messaging.handler.request.RequestMessageHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * Providing a MessageDispatcher as a bean, for autowiring.
 */
@Slf4j
@Component
public class MessageDispatcherProvider {
    /**
     * Make use of autowiring to get the parameters for the MessageDispatchers constructor and
     * create an Instance of MessageDispatcher with them.
     *
     * @param objectMapper    for parsing objects from json
     * @param provider        providing DAPS public key for checking DAT Tokens
     * @param configContainer container for current configuration
     * @param resolver        resolver for finding the right handler for infomodel {@link de.fraunhofer.iais.eis.Message}
     *
     * @return MessageDispatcher as Spring Bean
     */
    @Bean
    public MessageDispatcher provideMessageDispatcher(final ObjectMapper objectMapper,
                                                      final RequestMessageHandler resolver,
                                                      final DapsPublicKeyProvider provider,
                                                      final ConfigContainer configContainer,
                                                      final DapsValidator dapsValidator) {

        return new MessageDispatcher(objectMapper, resolver, provider, configContainer, dapsValidator);
    }
}
