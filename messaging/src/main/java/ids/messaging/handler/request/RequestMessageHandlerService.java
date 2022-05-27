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
package ids.messaging.handler.request;

import java.util.Arrays;
import java.util.Optional;

import de.fraunhofer.iais.eis.Message;
import ids.messaging.handler.message.MessageHandler;
import ids.messaging.handler.message.SupportedMessageType;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

/**
 * Resolver that uses the Spring dependency injection mechanism to
 * find the matching message handler.
 */
@Service
public class RequestMessageHandlerService implements RequestMessageHandler {

    /**
     * The ApplicationContext.
     */
    private final ApplicationContext appContext;

    /**
     * Default constructor autowired by Spring and sets ApplicationContext from Spring.
     *
     * @param appContext context to access Spring CDI
     */
    @Autowired
    public RequestMessageHandlerService(final ApplicationContext appContext) {
        this.appContext = appContext;
    }

    /**
     * Resolve a MessageHandler instance that is able to handle the given messageType parameter.
     *
     * @param messageType type of the message to handle
     * @param <R>         generic constraint to get a subtype of RequestMessage
     * @return optionally found matching handler instance
     */
    @Override
    @SuppressWarnings("unchecked")
    public <R extends Message> Optional<MessageHandler<R>> resolveHandler(
            final Class<R> messageType) {
        return Arrays.stream(appContext.getBeanNamesForAnnotation(SupportedMessageType.class))
                     .flatMap(s -> Optional.ofNullable(
                             appContext.findAnnotationOnBean(s, SupportedMessageType.class))
                                           .stream().map(msg -> new Tuple<>(s, msg)))
                     .filter(t -> t.value.value().equals(messageType))
                .<MessageHandler<R>>map(t -> appContext.getBean(t.key, MessageHandler.class))
                .findFirst();
    }


    @Data
    @RequiredArgsConstructor
    private static class Tuple<K, V> {

        /**
         * Key of the tuple.
         */
        private final K key;

        /**
         * Value of the tuple.
         */
        private final V value;
    }
}
