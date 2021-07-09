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
import de.fraunhofer.ids.messaging.requests.enums.Crud;
import de.fraunhofer.ids.messaging.requests.enums.ProtocolType;
import de.fraunhofer.ids.messaging.requests.exceptions.RejectionException;
import de.fraunhofer.ids.messaging.requests.exceptions.UnexpectedPayloadException;

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
    protected Optional<Object> optPayload;
    protected Optional<Class<T>> expectedPayload;
    protected boolean throwOnRejection;
    protected Crud operation;

    /**
     * IDS request, expecting payload of type 'expected'.
     *
     * @param expected expected Type of payload
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

    /**
     * Choose 'artifact' as the subject of the message.
     *
     * @return an ArtifactRequestBuilder with current information of this builder
     */
    public ArtifactRequestBuilder<T> subjectArtifact() {
        var builder = new ArtifactRequestBuilder<>(expectedPayload.orElse(null), messageService, requestTemplateProvider, notificationTemplateProvider)
                .withPayload(protocolType);
        return this.throwOnRejection ? builder.throwOnRejection() : builder;
    }

    /**
     * Choose 'connector' as the subject of the message.
     *
     * @return an ConnectorRequestBuilder with current information of this builder
     */
    public ConnectorRequestBuilder<T> subjectConnector() {
        var builder = new ConnectorRequestBuilder<>(expectedPayload.orElse(null), messageService, requestTemplateProvider, notificationTemplateProvider);
        return this.throwOnRejection ? builder.throwOnRejection() : builder;
    }

    /**
     * Choose 'resource' as the subject of the message.
     *
     * @return an ResourceRequestBuilder with current information of this builder
     */
    public ResourceRequestBuilder<T> subjectResource() {
        var builder = new ResourceRequestBuilder<>(expectedPayload.orElse(null), messageService, requestTemplateProvider, notificationTemplateProvider);
        return this.throwOnRejection ? builder.throwOnRejection() : builder;
    }

    /**
     * Choose 'query' as the subject of the message.
     *
     * @return an QueryRequestBuilder with current information of this builder
     */
    public QueryRequestBuilder<T> subjectQuery() {
        var builder = new QueryRequestBuilder<>(expectedPayload.orElse(null), messageService, requestTemplateProvider, notificationTemplateProvider)
                .withPayload(protocolType);
        return this.throwOnRejection ? builder.throwOnRejection() : builder;
    }

    /**
     * Choose 'participant' as the subject of the message.
     *
     * @return an ParticipantRequestBuilder with current information of this builder
     */
    public ParticipantRequestBuilder<T> subjectParticipant() {
        var builder = new ParticipantRequestBuilder<>(expectedPayload.orElse(null), messageService, requestTemplateProvider, notificationTemplateProvider)
                .withPayload(protocolType);
        return this.throwOnRejection ? builder.throwOnRejection() : builder;
    }

    /**
     * Choose 'app' as the subject of the message.
     *
     * @return an AppRequestBuilder with current information of this builder
     */
    public AppRequestBuilder<T> subjectApp() {
        var builder = new AppRequestBuilder<>(expectedPayload.orElse(null), messageService, requestTemplateProvider, notificationTemplateProvider)
                .withPayload(protocolType);
        return this.throwOnRejection ? builder.throwOnRejection() : builder;
    }

    /**
     * Choose 'description' as the subject of the message.
     *
     * @return an DescriptionRequestBuilder with current information of this builder
     */
    public DescriptionRequestBuilder<T> subjectDescription() {
        var builder = new DescriptionRequestBuilder<>(expectedPayload.orElse(null), messageService, requestTemplateProvider, notificationTemplateProvider)
                .withPayload(protocolType);
        return this.throwOnRejection ? builder.throwOnRejection() : builder;
    }

    /**
     * Choose 'log' as the subject of the message.
     *
     * @return an LogRequestBuilder with current information of this builder
     */
    public LogRequestBuilder<T> subjectLog() {
        var builder = new LogRequestBuilder<>(expectedPayload.orElse(null), messageService, requestTemplateProvider, notificationTemplateProvider)
                .withPayload(protocolType);
        return this.throwOnRejection ? builder.throwOnRejection() : builder;
    }

    /**
     * Choose 'connector certificate' as the subject of the message.
     *
     * @return an ConnectorCertificateRequestBuilder with current information of this builder
     */
    public ConnectorCertificateRequestBuilder<T> subjectConnectorCertificate() {
        var builder = new ConnectorCertificateRequestBuilder<>(expectedPayload.orElse(null), messageService, requestTemplateProvider, notificationTemplateProvider)
                .withPayload(protocolType);
        return this.throwOnRejection ? builder.throwOnRejection() : builder;
    }

    /**
     * Choose 'participant' as the subject of the message.
     *
     * @return an ParticipantCertificateRequestBuilder with current information of this builder
     */
   public ParticipantCertificateRequestBuilder<T> subjectParticipantCertificate() {
       var builder = new ParticipantCertificateRequestBuilder<>(expectedPayload.orElse(null), messageService, requestTemplateProvider, notificationTemplateProvider)
               .withPayload(protocolType);
       return this.throwOnRejection ? builder.throwOnRejection() : builder;
   }

    /**
     * Choose 'contract' as the subject of the message.
     *
     * @return an ContractRequestBuilder with current information of this builder
     */
   public ContractRequestBuilder<T> subjectContract() {
       var builder = new ContractRequestBuilder<>(expectedPayload.orElse(null), messageService, requestTemplateProvider, notificationTemplateProvider)
               .withPayload(protocolType);
       return this.throwOnRejection ? builder.throwOnRejection() : builder;
   }

    /**
     * Choose 'command' as the subject of the message.
     *
     * @return an CommandRequestBuilder with current information of this builder
     */
   public CommandRequestBuilder<T> subjectCommand() {
       var builder = new CommandRequestBuilder<>(expectedPayload.orElse(null), messageService, requestTemplateProvider, notificationTemplateProvider)
               .withPayload(protocolType);
       return this.throwOnRejection ? builder.throwOnRejection() : builder;
   }

    /**
     * Choose 'contract supplement' as the subject of the message.
     *
     * @return an ContractSupplementRequestBuilder with current information of this builder
     */
   public ContractSupplementRequestBuilder<T> subjectContractSupplement() {
       var builder = new ContractSupplementRequestBuilder<>(expectedPayload.orElse(null), messageService, requestTemplateProvider, notificationTemplateProvider)
               .withPayload(protocolType);
       return this.throwOnRejection ? builder.throwOnRejection() : builder;
   }

    /**
     * Choose 'contract offer' as the subject of the message.
     *
     * @return an ContractOfferRequestBuilder with current information of this builder
     */
    public ContractOfferRequestBuilder<T> subjectContractOffer() {
        var builder = new ContractOfferRequestBuilder<>(expectedPayload.orElse(null), messageService, requestTemplateProvider, notificationTemplateProvider)
                .withPayload(protocolType);
        return this.throwOnRejection ? builder.throwOnRejection() : builder;
    }

    /**
     * Choose 'access token' as the subject of the message.
     *
     * @return an AccessTokenRequestBuilder with current information of this builder
     */
    public AccessTokenRequestBuilder<T> subjectAccessToken() {
        var builder = new AccessTokenRequestBuilder<>(expectedPayload.orElse(null), messageService, requestTemplateProvider, notificationTemplateProvider)
                .withPayload(protocolType);
        return this.throwOnRejection ? builder.throwOnRejection() : builder;
    }

    /**
     * Send a multipart message to target using current information of the RequestBuilder.
     *
     * @param target target URI message will be sent to
     * @param message multipart header message
     * @return MessageContainer containing response
     * @throws RejectionException when response is a RejectionMessage (and 'throwOnRejection' is set in the builder)
     * @throws UnexpectedPayloadException when payload is not of type T
     * @throws ShaclValidatorException when Shacl Validation fails
     * @throws SerializeException when the payload cannot be serialized
     * @throws ClaimsException when DAT of response is not valid
     * @throws UnknownResponseException when type of response is not known
     * @throws SendMessageException when an IOException is thrown by the httpclient when sending the message
     * @throws MultipartParseException when the response cannot be parsed as multipart
     * @throws IOException when some other error happens while sending the message
     * @throws DeserializeException when response cannot be deserialized
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
