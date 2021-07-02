package de.fraunhofer.ids.messaging.requests.builder;

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.RejectionMessage;
import de.fraunhofer.ids.messaging.common.DeserializeException;
import de.fraunhofer.ids.messaging.common.SerializeException;
import de.fraunhofer.ids.messaging.core.daps.ClaimsException;
import de.fraunhofer.ids.messaging.protocol.MessageService;
import de.fraunhofer.ids.messaging.protocol.http.SendMessageException;
import de.fraunhofer.ids.messaging.protocol.http.ShaclValidatorException;
import de.fraunhofer.ids.messaging.protocol.multipart.UnknownResponseException;
import de.fraunhofer.ids.messaging.protocol.multipart.mapping.GenericMessageAndPayload;
import de.fraunhofer.ids.messaging.protocol.multipart.parser.MultipartParseException;
import de.fraunhofer.ids.messaging.requests.MessageContainer;
import de.fraunhofer.ids.messaging.requests.NotificationTemplateProvider;
import de.fraunhofer.ids.messaging.requests.RequestTemplateProvider;
import de.fraunhofer.ids.messaging.requests.enums.ProtocolType;
import de.fraunhofer.ids.messaging.requests.exceptions.RejectionException;
import de.fraunhofer.ids.messaging.requests.exceptions.UnexpectedPayloadException;
import lombok.extern.java.Log;
import org.checkerframework.checker.units.qual.C;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;

/**
 * Builder class for configurable ids requests.
 *
 * @param <T> type of payload
 */
public class IdsRequestBuilder<T> {

    protected MessageService messageService;
    protected RequestTemplateProvider requestTemplateProvider;
    protected NotificationTemplateProvider notificationTemplateProvider;

    protected ProtocolType protocolType;

    /**
     * Optional: payload, when payload should be sent with message.
     */
    protected Optional<Object> optPayload;

    /**
     * Optional: expected payload type, will be checked when receiving a response.
     */
    protected Optional<Class<T>> expectedPayload;

    /**
     * Boolean flag, if set to true a RejectionMessage will lead to an {@link RejectionException}.
     */
    protected boolean throwOnRejection;

    /**
     * IDS request, expecting payload of type 'expected'.
     *
     * @param expected expected Type of payload
     */
    IdsRequestBuilder(final Class<T> expected, ProtocolType protocolType, MessageService messageService, RequestTemplateProvider requestTemplateProvider, NotificationTemplateProvider notificationTemplateProvider) {
        this.expectedPayload = Optional.ofNullable(expected);
        this.optPayload = Optional.empty();
        this.protocolType = protocolType;
        this.messageService = messageService;
        this.requestTemplateProvider = requestTemplateProvider;
        this.notificationTemplateProvider = notificationTemplateProvider;
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

    /**
     * If this is set, a RejectionMessage as response will trigger a {@link RejectionException}.
     *
     * @return this builder instance
     */
    public IdsRequestBuilder<T> throwOnRejection() {
        this.throwOnRejection = true;
        return this;
    }

    public ArtifactRequestBuilder<T> subjectArtifact(){
        var builder = new ArtifactRequestBuilder<>(expectedPayload.orElse(null), protocolType, messageService, requestTemplateProvider, notificationTemplateProvider)
                .withPayload(protocolType);
        return this.throwOnRejection ? builder.throwOnRejection() : builder;
    }

    public ConnectorRequestBuilder<T> subjectConnector(){
        var builder = new ConnectorRequestBuilder<>(expectedPayload.orElse(null), protocolType, messageService, requestTemplateProvider, notificationTemplateProvider);
        return this.throwOnRejection ? builder.throwOnRejection() : builder;
    }

    public ResourceRequestBuilder<T> subjectResource(){
        var builder = new ResourceRequestBuilder<>(expectedPayload.orElse(null), protocolType, messageService, requestTemplateProvider, notificationTemplateProvider);
        return this.throwOnRejection ? builder.throwOnRejection() : builder;
    }

    public QueryRequestBuilder<T> subjectQuery(){
        var builder = new QueryRequestBuilder<>(expectedPayload.orElse(null), protocolType, messageService, requestTemplateProvider, notificationTemplateProvider)
                .withPayload(protocolType);
        return this.throwOnRejection ? builder.throwOnRejection() : builder;
    }

    public ParticipantRequestBuilder<T> subjectParticipant(){
        var builder = new ParticipantRequestBuilder<>(expectedPayload.orElse(null), protocolType, messageService, requestTemplateProvider, notificationTemplateProvider)
                .withPayload(protocolType);
        return this.throwOnRejection ? builder.throwOnRejection() : builder;
    }

    public AppRequestBuilder<T> subjectApp(){
        var builder = new AppRequestBuilder<>(expectedPayload.orElse(null), protocolType, messageService, requestTemplateProvider, notificationTemplateProvider)
                .withPayload(protocolType);
        return this.throwOnRejection ? builder.throwOnRejection() : builder;
    }

    public DescriptionRequestBuilder<T> subjectDescription(){
        var builder = new DescriptionRequestBuilder<>(expectedPayload.orElse(null), protocolType, messageService, requestTemplateProvider, notificationTemplateProvider)
                .withPayload(protocolType);
        return this.throwOnRejection ? builder.throwOnRejection() : builder;
    }

    public LogRequestBuilder<T> subjectLog(){
        var builder = new LogRequestBuilder<>(expectedPayload.orElse(null), protocolType, messageService, requestTemplateProvider, notificationTemplateProvider)
                .withPayload(protocolType);
        return this.throwOnRejection ? builder.throwOnRejection() : builder;
    }

    public ConnectorCertificateRequestBuilder<T> subjectConnectorCertificate(){
        var builder = new ConnectorCertificateRequestBuilder<>(expectedPayload.orElse(null), protocolType, messageService, requestTemplateProvider, notificationTemplateProvider)
                .withPayload(protocolType);
        return this.throwOnRejection ? builder.throwOnRejection() : builder;
    }

   public ParticipantCertificateRequestBuilder<T> subjectParticipantCertificate(){
       var builder = new ParticipantCertificateRequestBuilder<>(expectedPayload.orElse(null), protocolType, messageService, requestTemplateProvider, notificationTemplateProvider)
               .withPayload(protocolType);
       return this.throwOnRejection ? builder.throwOnRejection() : builder;
   }

   public ContractRequestBuilder<T> subjectContract(){
       var builder = new ContractRequestBuilder<>(expectedPayload.orElse(null), protocolType, messageService, requestTemplateProvider, notificationTemplateProvider)
               .withPayload(protocolType);
       return this.throwOnRejection ? builder.throwOnRejection() : builder;
   }

   public CommandRequestBuilder<T> subjectCommand(){
       var builder = new CommandRequestBuilder<>(expectedPayload.orElse(null), protocolType, messageService, requestTemplateProvider, notificationTemplateProvider)
               .withPayload(protocolType);
       return this.throwOnRejection ? builder.throwOnRejection() : builder;
   }

    protected MessageContainer<T> sendMultipart(URI target, Message message)
            throws RejectionException,
            UnexpectedPayloadException,
            ShaclValidatorException,
            SerializeException,
            ClaimsException,
            UnknownResponseException,
            SendMessageException,
            MultipartParseException,
            IOException,
            DeserializeException {
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
