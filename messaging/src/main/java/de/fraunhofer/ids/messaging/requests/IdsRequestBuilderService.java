package de.fraunhofer.ids.messaging.requests;

import de.fraunhofer.iais.eis.RejectionMessage;
import de.fraunhofer.ids.messaging.core.daps.ClaimsException;
import de.fraunhofer.ids.messaging.core.daps.DapsTokenManagerException;
import de.fraunhofer.ids.messaging.protocol.MessageService;
import de.fraunhofer.ids.messaging.protocol.multipart.mapping.GenericMessageAndPayload;
import de.fraunhofer.ids.messaging.protocol.multipart.parser.MultipartParseException;
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
 * Service for building and sending ids requests
 */
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PROTECTED)
@Service
public class IdsRequestBuilderService {

    MessageService messageService;

    public IdsRequestBuilder<Object> newRequest(){
        return new IdsRequestBuilder<>();
    }

    public <T> IdsRequestBuilder<T> newRequestExpectingType(Class<T> expected) {return new IdsRequestBuilder<>(expected);}

    /**
     * Builder class for configurable ids requests
     *
     * @param <T> type of payload
     */
    class IdsRequestBuilder<T> {

        private RequestMessageTemplate<?> requestMessageTemplate;
        private Optional<Object> payload;
        private Optional<Class<T>> expectedPayload;
        private boolean throwOnRejection;

        /**
         * Generic IDS request, expecting no specific type of payload
         */
        public IdsRequestBuilder() {
            this.expectedPayload = Optional.empty();
            this.payload = Optional.empty();
        }

        /**
         * IDS request, expecting payload of type 'expected'
         *
         * @param expected expected Type of payload
         */
        public IdsRequestBuilder(Class<T> expected) {
            this.expectedPayload = Optional.of(expected);
            this.payload = Optional.empty();
        }

        /**
         * Add a payload to the request
         *
         * @param payload payload to be sent with the request
         * @return this builder instance
         */
        public IdsRequestBuilder<T> withPayload(Object payload){
            this.payload = Optional.ofNullable(payload);
            return this;
        }

        /**
         * Choose a header template to use for the request
         *
         * @param template header template to use for the request
         * @return this builder instance
         */
        public IdsRequestBuilder<T> useTemplate(RequestMessageTemplate<?> template){
            this.requestMessageTemplate = template;
            return this;
        }

        /**
         * If this is set, a RejectionMessage as response will trigger a {@link RejectionException}
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
         * @throws IOException when there is an error while sending the message
         * @throws DapsTokenManagerException when DAT cannot be received from DAPS
         * @throws NoTemplateProvidedException when no Message Template is provided
         * @throws RejectionException when 'throwOnRejection' is active and RejectionMessage is received
         * @throws UnexpectedPayloadException when 'expectedPayload' is set and response payload has a different type
         */
        public MessageContainer<T> execute(URI target) throws ClaimsException, MultipartParseException, IOException, DapsTokenManagerException, NoTemplateProvidedException, RejectionException, UnexpectedPayloadException {
            if(requestMessageTemplate == null) throw new NoTemplateProvidedException("No Message Template was Provided!");
            var message = requestMessageTemplate.buildMessage();
            final var messageAndPayload = new GenericMessageAndPayload(message, payload.orElse(null));
            final var response = messageService.sendIdsMessage(messageAndPayload, target);
            var header = response.getMessage();
            var payload = response.getPayload().orElse(null);
            if(throwOnRejection){
                if (header instanceof RejectionMessage) throw new RejectionException(String.format("Message was Rejected! Reason: %s", payload), ((RejectionMessage) header).getRejectionReason());
            }
            if(expectedPayload.isPresent()){
                if(payload == null || !expectedPayload.get().isAssignableFrom(payload.getClass())){
                    throw new UnexpectedPayloadException(String.format("Expected payload of type %s but received %s!", expectedPayload.get(), payload == null ? "null" : payload.getClass()), new MessageContainer<>(header, payload));
                }else{
                    return new MessageContainer<>(header, (T) payload);
                }
            }
            return new MessageContainer<>(header, (T) payload);
        }
    }
}
