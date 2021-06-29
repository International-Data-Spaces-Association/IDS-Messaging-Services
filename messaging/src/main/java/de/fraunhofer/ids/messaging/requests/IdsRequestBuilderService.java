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
import de.fraunhofer.ids.messaging.common.DeserializeException;
import de.fraunhofer.ids.messaging.common.SerializeException;
import de.fraunhofer.ids.messaging.core.daps.ClaimsException;
import de.fraunhofer.ids.messaging.core.daps.DapsTokenManagerException;
import de.fraunhofer.ids.messaging.protocol.MessageService;
import de.fraunhofer.ids.messaging.requests.enums.ProtocolType;
import de.fraunhofer.ids.messaging.protocol.http.SendMessageException;
import de.fraunhofer.ids.messaging.protocol.http.ShaclValidatorException;
import de.fraunhofer.ids.messaging.protocol.multipart.UnknownResponseException;
import de.fraunhofer.ids.messaging.protocol.multipart.mapping.GenericMessageAndPayload;
import de.fraunhofer.ids.messaging.protocol.multipart.parser.MultipartParseException;
import de.fraunhofer.ids.messaging.requests.enums.Crud;
import de.fraunhofer.ids.messaging.requests.enums.Subject;
import de.fraunhofer.ids.messaging.requests.exceptions.NoTemplateProvidedException;
import de.fraunhofer.ids.messaging.requests.exceptions.RejectionException;
import de.fraunhofer.ids.messaging.requests.exceptions.UnexpectedPayloadException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;

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
    TemplateResolveService templateResolveService;

    /**
     * Get a requestbuilder, expecting no specific payload type.
     *
     * @return an {@link IdsRequestBuilder} expecting no specific payload type.
     */
    public IdsRequestBuilder<Object> newRequest(final ProtocolType protocolType) {
        return new IdsRequestBuilder<>(protocolType);
    }

    /**
     * Get a requestbuilder, expecting payload of type T.
     *
     * @param expected expected class of payload object.
     * @param <T> expected type of payload.
     * @return an {@link IdsRequestBuilder} expecting payload of type T.
     */
    public <T> IdsRequestBuilder<T> newRequestExpectingType(final Class<T> expected, final ProtocolType protocolType) {
        return new IdsRequestBuilder<>(expected, protocolType);
    }

    /**
     * Builder class for configurable ids requests.
     *
     * @param <T> type of payload
     */
    public class IdsRequestBuilder<T> {

        private Subject subject;
        private Crud operation;
        private ProtocolType protocolType;


        /**
         * Optional: payload, when payload should be sent with message.
         */
        private Optional<Object> optPayload;

        /**
         * Optional: expected payload type, will be checked when receiving a response.
         */
        private Optional<Class<T>> expectedPayload;

        /**
         * Boolean flag, if set to true a RejectionMessage will lead to an {@link RejectionException}.
         */
        private boolean throwOnRejection;

        /**
         * Generic IDS request, expecting no specific type of payload.
         */
        IdsRequestBuilder(ProtocolType protocolType) {
            this.expectedPayload = Optional.empty();
            this.optPayload = Optional.empty();
            this.protocolType = protocolType;
        }

        /**
         * IDS request, expecting payload of type 'expected'.
         *
         * @param expected expected Type of payload
         */
        IdsRequestBuilder(final Class<T> expected, ProtocolType protocolType) {
            this.expectedPayload = Optional.of(expected);
            this.optPayload = Optional.empty();
            this.protocolType = protocolType;
        }

        /**
         * Add a payload to the request.
         *
         * @param payload payload to be sent with the request
         * @return this builder instance
         */
        public IdsRequestBuilder<T> withPayload(final Object payload) {
            this.optPayload = Optional.ofNullable(payload);
            return this;
        }

        public IdsRequestBuilder<T> withSubject(final Subject subject){
            this.subject = subject;
            return this;
        }

        public IdsRequestBuilder<T> withOperation(final Crud operation){
            this.operation = operation;
            return this;
        }

        /**
         * If this is set, a RejectionMessage as response will trigger a {@link RejectionException}.
         *
         * @return this builder instance
         */
        public IdsRequestBuilder<T> throwOnRejection() {
            this.throwOnRejection = true;
            return this;
        }

        /**
         * @param target target connector message will be sent to
         * @return response of target connector bundled in {@link MessageContainer}
         * @throws ClaimsException when incoming DAT cannot be parsed
         * @throws MultipartParseException when response cannot be parsed to multipart
         * @throws DapsTokenManagerException when DAT cannot be received from DAPS
         * @throws NoTemplateProvidedException when no Message Template is provided
         * @throws RejectionException when 'throwOnRejection' is active and RejectionMessage is received
         * @throws UnexpectedPayloadException when 'expectedPayload' is set and response payload has a different type
         */
        public MessageContainer<T> execute(final URI target) throws
                ClaimsException,
                MultipartParseException,
                IOException,
                DapsTokenManagerException,
                NoTemplateProvidedException,
                RejectionException,
                UnexpectedPayloadException,
                ShaclValidatorException,
                SerializeException,
                UnknownResponseException,
                SendMessageException,
                DeserializeException {
            switch (protocolType) {
                case LDP:
                    //TODO send via IDS_LDP
                    throw new UnsupportedOperationException("Not yet implemented!");
                case IDSCP:
                    throw new UnsupportedOperationException("Not yet implemented!");
                case MULTIPART:
                    //send via multipart
                    var template = templateResolveService.provideMessageTemplate(subject, operation);
                    if(template == null) throw new NoTemplateProvidedException();
                    return sendMultipart(template, target);
            }
            return null;
        }

        private MessageContainer<T> sendMultipart(MessageTemplate template, URI target)
                throws RejectionException,
                UnexpectedPayloadException,
                DapsTokenManagerException,
                ShaclValidatorException,
                SerializeException,
                ClaimsException,
                UnknownResponseException,
                SendMessageException,
                MultipartParseException,
                IOException,
                DeserializeException {
            var message = template.buildMessage();
            final var messageAndPayload = new GenericMessageAndPayload(message, optPayload.orElse(null));
            final var response = messageService.sendIdsMessage(messageAndPayload, target);
            var header = response.getMessage();
            var payload = response.getPayload().orElse(null);
            if (throwOnRejection) {
                if (header instanceof RejectionMessage) {
                    throw new RejectionException(
                            String.format("Message was Rejected! Reason: %s", payload),
                            ((RejectionMessage) header).getRejectionReason()
                    );
                }
            }
            if (expectedPayload.isPresent()) {
                if (payload == null || !expectedPayload.get().isAssignableFrom(payload.getClass())) {
                    throw new UnexpectedPayloadException(
                            String.format(
                                    "Expected payload of type %s but received %s!",
                                    expectedPayload.get(),
                                    payload == null ? "null" : payload.getClass()
                            ),
                            new MessageContainer<>(header, payload));
                } else {
                    return new MessageContainer<>(header, (T) payload);
                }
            }
            return new MessageContainer<>(header, (T) payload);
        }
    }
}
