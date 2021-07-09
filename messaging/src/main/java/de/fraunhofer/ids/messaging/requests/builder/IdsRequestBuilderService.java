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
package de.fraunhofer.ids.messaging.requests.builder;

import de.fraunhofer.ids.messaging.protocol.MessageService;
import de.fraunhofer.ids.messaging.requests.NotificationTemplateProvider;
import de.fraunhofer.ids.messaging.requests.RequestTemplateProvider;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service for building and sending ids requests.
 */
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Service
public class IdsRequestBuilderService {

    /**
     * {@link MessageService} used for sending messages.
     */
    MessageService messageService;
    RequestTemplateProvider requestTemplateProvider;
    NotificationTemplateProvider notificationTemplateProvider;

    /**
     * Get a requestbuilder, expecting no specific payload type.
     *
     * @return an {@link IdsRequestBuilder} expecting no specific payload type.
     */
    public IdsRequestBuilder<Object> newRequest() {
        return new IdsRequestBuilder<>(null, messageService, requestTemplateProvider, notificationTemplateProvider);
    }

    /**
     * Get a requestbuilder, expecting payload of type T.
     *
     * @param expected expected class of payload object.
     * @param <T> expected type of payload.
     * @return an {@link IdsRequestBuilder} expecting payload of type T.
     */
    public <T> IdsRequestBuilder<T> newRequestExpectingType(final Class<T> expected) {
        return new IdsRequestBuilder<>(expected, messageService, requestTemplateProvider, notificationTemplateProvider);
    }


}
