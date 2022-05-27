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
package ids.messaging.requests.builder;

import ids.messaging.protocol.MessageService;
import ids.messaging.requests.NotificationTemplateProvider;
import ids.messaging.requests.RequestTemplateProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service for building and sending ids requests.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IdsRequestBuilderService {

    /**
     * {@link MessageService} used for sending messages.
     */
    private final MessageService messageService;

    /**
     * The RequestTemplateProvider.
     */
    private final RequestTemplateProvider requestTemplateProvider;

    /**
     * The NotificationTemplateProvider.
     */
    private final NotificationTemplateProvider notificationTemplateProvider;

    /**
     * Get a requestbuilder, expecting no specific payload type.
     *
     * @return an {@link IdsRequestBuilder} Expecting no specific payload type.
     */
    public IdsRequestBuilder<Object> newRequest() {
        return new IdsRequestBuilder<>(null,
                                       messageService,
                                       requestTemplateProvider,
                                       notificationTemplateProvider);
    }

    /**
     * Get a requestbuilder, expecting payload of type T.
     *
     * @param expected Expected class of payload object.
     * @param <T> Expected type of payload.
     * @return An {@link IdsRequestBuilder} expecting payload of type T.
     */
    public <T> IdsRequestBuilder<T> newRequestExpectingType(final Class<T> expected) {
        return new IdsRequestBuilder<>(expected,
                                       messageService,
                                       requestTemplateProvider,
                                       notificationTemplateProvider);
    }
}
