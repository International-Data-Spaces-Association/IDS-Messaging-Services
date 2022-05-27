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

import java.io.IOException;
import java.net.URI;
import java.util.Optional;

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.RejectionMessage;
import ids.messaging.common.DeserializeException;
import ids.messaging.common.SerializeException;
import ids.messaging.core.daps.ClaimsException;
import ids.messaging.protocol.MessageService;
import ids.messaging.protocol.http.SendMessageException;
import ids.messaging.protocol.http.ShaclValidatorException;
import ids.messaging.protocol.multipart.UnknownResponseException;
import ids.messaging.protocol.multipart.mapping.GenericMessageAndPayload;
import ids.messaging.protocol.multipart.parser.MultipartParseException;
import ids.messaging.requests.MessageContainer;
import ids.messaging.requests.NotificationTemplateProvider;
import ids.messaging.requests.RequestTemplateProvider;
import ids.messaging.requests.enums.Crud;
import ids.messaging.requests.enums.ProtocolType;
import ids.messaging.requests.exceptions.RejectionException;
import ids.messaging.requests.exceptions.UnexpectedPayloadException;

/**
 * Builder class for configurable ids requests.
 *
 * @param <T> Type of payload.
 */
public class IdsRequestBuilder<T> {

    /**
     * The MessageService.
     */
    protected MessageService messageService;

    /**
     * The RequestTemplateProvider.
     */
    protected RequestTemplateProvider requestTemplateProvider;

    /**
     * The NotificationTemplateProvider.
     */
    protected NotificationTemplateProvider notificationTemplateProvider;

    /**
     * The chosen protocol type.
     */
    protected ProtocolType protocolType;

    /**
     * The payload.
     */
    protected Optional<Object> optPayload;

    /**
     * Expected Payload.
     */
    protected Optional<Class<T>> expectedPayload;

    /**
     * Boolean of exception should be thrown upon receiving a RejectionMessage.
     */
    protected boolean throwOnRejection;

    /**
     * The chosen CRUD operation.
     */
    protected Crud operation;

    /**
     * IDS request, expecting payload of type 'expected'.
     *
     * @param expected Expected Type of payload.
     * @param messageService The messageService.
     * @param notificationTemplateProvider The NotificationTemplateProvider.
     * @param requestTemplateProvider The RequestTemplateProvider.
     */
    IdsRequestBuilder(
            final Class<T> expected,
            final MessageService messageService,
            final RequestTemplateProvider requestTemplateProvider,
            final NotificationTemplateProvider notificationTemplateProvider) {
        this.expectedPayload = Optional.ofNullable(expected);
        this.optPayload = Optional.empty();
        this.messageService = messageService;
        this.requestTemplateProvider = requestTemplateProvider;
        this.notificationTemplateProvider = notificationTemplateProvider;
    }

    /**
     * Add a payload to the request.
     *
     * @param payload Payload to be sent with the request.
     * @return This builder instance.
     */
    public IdsRequestBuilder<T> withPayload(final Object payload) {
        this.optPayload = Optional.ofNullable(payload);
        return this;
    }

    /**
     * If this is set, a RejectionMessage as response will trigger a {@link RejectionException}.
     *
     * @return This builder instance.
     */
    public IdsRequestBuilder<T> throwOnRejection() {
        this.throwOnRejection = true;
        return this;
    }

    /**
     * Choose 'artifact' as the subject of the message.
     *
     * @return An ArtifactRequestBuilder with current information of this builder.
     */
    public ArtifactRequestBuilder<T> subjectArtifact() {
        final var builder = new ArtifactRequestBuilder<>(expectedPayload.orElse(null),
                   messageService,
                   requestTemplateProvider,
                   notificationTemplateProvider)
                .withPayload(optPayload.orElse(null));
        return this.throwOnRejection ? builder.throwOnRejection() : builder;
    }

    /**
     * Choose 'connector' as the subject of the message.
     *
     * @return An ConnectorRequestBuilder with current information of this builder.
     */
    public ConnectorRequestBuilder<T> subjectConnector() {
        final var builder =
                new ConnectorRequestBuilder<>(expectedPayload.orElse(null),
                  messageService,
                  requestTemplateProvider,
                  notificationTemplateProvider)
            .withPayload(optPayload.orElse(null));
        return this.throwOnRejection ? builder.throwOnRejection() : builder;
    }

    /**
     * Choose 'resource' as the subject of the message.
     *
     * @return An ResourceRequestBuilder with current information of this builder.
     */
    public ResourceRequestBuilder<T> subjectResource() {
        final var builder = new ResourceRequestBuilder<>(expectedPayload.orElse(null),
                   messageService,
                   requestTemplateProvider,
                   notificationTemplateProvider)
                .withPayload(optPayload.orElse(null));
        return this.throwOnRejection ? builder.throwOnRejection() : builder;
    }

    /**
     * Choose 'query' as the subject of the message.
     *
     * @return An QueryRequestBuilder with current information of this builder.
     */
    public QueryRequestBuilder<T> subjectQuery() {
        final var builder = new QueryRequestBuilder<>(expectedPayload.orElse(null),
                    messageService,
                    requestTemplateProvider,
                    notificationTemplateProvider)
                .withPayload(optPayload.orElse(null));
        return this.throwOnRejection ? builder.throwOnRejection() : builder;
    }

    /**
     * Choose 'participant' as the subject of the message.
     *
     * @return An ParticipantRequestBuilder with current information of this builder.
     */
    public ParticipantRequestBuilder<T> subjectParticipant() {
        final var builder =
                new ParticipantRequestBuilder<>(expectedPayload.orElse(null),
                    messageService,
                    requestTemplateProvider,
                    notificationTemplateProvider)
                .withPayload(optPayload.orElse(null));
        return this.throwOnRejection ? builder.throwOnRejection() : builder;
    }

    /**
     * Choose 'app' as the subject of the message.
     *
     * @return An AppRequestBuilder with current information of this builder.
     */
    public AppRequestBuilder<T> subjectApp() {
        final var builder = new AppRequestBuilder<>(expectedPayload.orElse(null),
                  messageService,
                  requestTemplateProvider,
                  notificationTemplateProvider)
                .withPayload(optPayload.orElse(null));
        return this.throwOnRejection ? builder.throwOnRejection() : builder;
    }

    /**
     * Choose 'description' as the subject of the message.
     *
     * @return An DescriptionRequestBuilder with current information of this builder.
     */
    public DescriptionRequestBuilder<T> subjectDescription() {
        final var builder =
                new DescriptionRequestBuilder<>(expectedPayload.orElse(null),
                    messageService,
                    requestTemplateProvider,
                    notificationTemplateProvider)
                .withPayload(optPayload.orElse(null));
        return this.throwOnRejection ? builder.throwOnRejection() : builder;
    }

    /**
     * Choose 'log' as the subject of the message.
     *
     * @return An LogRequestBuilder with current information of this builder.
     */
    public LogRequestBuilder<T> subjectLog() {
        final var builder = new LogRequestBuilder<>(expectedPayload.orElse(null),
                  messageService,
                  requestTemplateProvider,
                  notificationTemplateProvider)
                .withPayload(optPayload.orElse(null));
        return this.throwOnRejection ? builder.throwOnRejection() : builder;
    }

    /**
     * Choose 'connector certificate' as the subject of the message.
     *
     * @return An ConnectorCertificateRequestBuilder with current information of this builder.
     */
    public ConnectorCertificateRequestBuilder<T> subjectConnectorCertificate() {
        final var builder = new ConnectorCertificateRequestBuilder<>(
                    expectedPayload.orElse(null),
                    messageService,
                    requestTemplateProvider,
                    notificationTemplateProvider)
                .withPayload(optPayload.orElse(null));
        return this.throwOnRejection ? builder.throwOnRejection() : builder;
    }

    /**
     * Choose 'participant' as the subject of the message.
     *
     * @return An ParticipantCertificateRequestBuilder with current information of this builder.
     */
   public ParticipantCertificateRequestBuilder<T> subjectParticipantCertificate() {
       final var builder = new ParticipantCertificateRequestBuilder<>(
                   expectedPayload.orElse(null),
                   messageService,
                   requestTemplateProvider,
                   notificationTemplateProvider)
               .withPayload(optPayload.orElse(null));
       return this.throwOnRejection ? builder.throwOnRejection() : builder;
   }

    /**
     * Choose 'contract' as the subject of the message.
     *
     * @return An ContractRequestBuilder with current information of this builder.
     */
   public ContractRequestBuilder<T> subjectContract() {
       final var builder = new ContractRequestBuilder<>(expectedPayload.orElse(null),
                                                  messageService,
                                                  requestTemplateProvider,
                                                  notificationTemplateProvider)
               .withPayload(optPayload.orElse(null));
       return this.throwOnRejection ? builder.throwOnRejection() : builder;
   }

    /**
     * Choose 'command' as the subject of the message.
     *
     * @return An CommandRequestBuilder with current information of this builder.
     */
   public CommandRequestBuilder<T> subjectCommand() {
       final var builder = new CommandRequestBuilder<>(expectedPayload.orElse(null),
                                                 messageService,
                                                 requestTemplateProvider,
                                                 notificationTemplateProvider)
               .withPayload(optPayload.orElse(null));
       return this.throwOnRejection ? builder.throwOnRejection() : builder;
   }

    /**
     * Choose 'contract supplement' as the subject of the message.
     *
     * @return An ContractSupplementRequestBuilder with current information of this builder.
     */
   public ContractSupplementRequestBuilder<T> subjectContractSupplement() {
       final var builder = new ContractSupplementRequestBuilder<>(
               expectedPayload.orElse(null),
               messageService,
               requestTemplateProvider,
               notificationTemplateProvider)
               .withPayload(optPayload.orElse(null));
       return this.throwOnRejection ? builder.throwOnRejection() : builder;
   }

    /**
     * Choose 'contract offer' as the subject of the message.
     *
     * @return An ContractOfferRequestBuilder with current information of this builder.
     */
    public ContractOfferRequestBuilder<T> subjectContractOffer() {
        final var builder =
                new ContractOfferRequestBuilder<>(expectedPayload.orElse(null),
                                                  messageService,
                                                  requestTemplateProvider,
                                                  notificationTemplateProvider)
                .withPayload(optPayload.orElse(null));
        return this.throwOnRejection ? builder.throwOnRejection() : builder;
    }

    /**
     * Choose 'access token' as the subject of the message.
     *
     * @return An AccessTokenRequestBuilder with current information of this builder.
     */
    public AccessTokenRequestBuilder<T> subjectAccessToken() {
        final var builder =
                new AccessTokenRequestBuilder<>(expectedPayload.orElse(null),
                                                messageService,
                                                requestTemplateProvider,
                                                notificationTemplateProvider)
                .withPayload(optPayload.orElse(null));
        return this.throwOnRejection ? builder.throwOnRejection() : builder;
    }

    /**
     * Send a multipart message to target using current information of the RequestBuilder.
     *
     * @param target Target URI message will be sent to.
     * @param message Multipart header message.
     * @return MessageContainer containing response.
     * @throws RejectionException When response is a RejectionMessage (and 'throwOnRejection'
     * is set in the builder).
     * @throws UnexpectedPayloadException When payload is not of type T.
     * @throws ShaclValidatorException When Shacl Validation fails.
     * @throws SerializeException When the payload cannot be serialized.
     * @throws ClaimsException When DAT of response is not valid.
     * @throws UnknownResponseException When type of response is not known.
     * @throws SendMessageException When Message cannot be sent, because
     * of missing fields or an error in the underlying httpclient
     * @throws MultipartParseException When the response cannot be parsed as multipart.
     * @throws IOException When some other error happens while sending the message.
     * @throws DeserializeException When response cannot be deserialized.
     */
    protected MessageContainer<T> sendMultipart(final URI target, final Message message)
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
        final var messageAndPayload =
                new GenericMessageAndPayload(message, optPayload.orElse(null));
        final var response = messageService.sendIdsMessage(messageAndPayload, target);
        final var header = response.getMessage();
        final var payload = response.getPayload().orElse(null);
        if (throwOnRejection && header instanceof RejectionMessage) {
            throw new RejectionException(
                    String.format("Message was Rejected! Reason: %s", payload),
                    ((RejectionMessage) header).getRejectionReason()
            );
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
