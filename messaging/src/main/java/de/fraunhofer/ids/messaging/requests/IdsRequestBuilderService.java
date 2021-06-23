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

    class IdsRequestBuilder<T> {

        private RequestMessageTemplate<?> requestMessageTemplate;
        private Optional<Class<T>> expectedPayload;
        private boolean throwOnRejection;

        public IdsRequestBuilder() {
            this.expectedPayload = Optional.empty();
        }

        public IdsRequestBuilder(Class<T> expected) {
            this.expectedPayload = Optional.of(expected);
        }

        public IdsRequestBuilder<T> useTemplate(RequestMessageTemplate<?> template){
            this.requestMessageTemplate = template;
            return this;
        }

        public IdsRequestBuilder<T> throwOnRejection() {
            this.throwOnRejection = true;
            return this;
        }

        public MessageContainer<T> execute(URI target) throws ClaimsException, MultipartParseException, IOException, DapsTokenManagerException, NoTemplateProvidedException, RejectionException, UnexpectedPayloadException {
            if(requestMessageTemplate == null) throw new NoTemplateProvidedException("No Message Template was Provided!");
            var message = requestMessageTemplate.buildMessage();
            final var messageAndPayload = new GenericMessageAndPayload(message);
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
