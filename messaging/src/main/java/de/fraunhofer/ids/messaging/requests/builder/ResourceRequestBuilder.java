package de.fraunhofer.ids.messaging.requests.builder;

import de.fraunhofer.ids.messaging.common.DeserializeException;
import de.fraunhofer.ids.messaging.common.SerializeException;
import de.fraunhofer.ids.messaging.core.daps.ClaimsException;
import de.fraunhofer.ids.messaging.core.daps.DapsTokenManagerException;
import de.fraunhofer.ids.messaging.protocol.MessageService;
import de.fraunhofer.ids.messaging.protocol.http.SendMessageException;
import de.fraunhofer.ids.messaging.protocol.http.ShaclValidatorException;
import de.fraunhofer.ids.messaging.protocol.multipart.UnknownResponseException;
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
 * RequestBuilder for messages with subject 'resource'.
 *
 * @param <T> Type of expected Payload.
 */
public class ResourceRequestBuilder<T> extends IdsRequestBuilder<T> implements ExecutableBuilder<T>, SupportsMultipart<T, ResourceRequestBuilder<T>> {

    private URI affectedResource;

    ResourceRequestBuilder(
            Class<T> expected,
            MessageService messageService,
            RequestTemplateProvider requestTemplateProvider,
            NotificationTemplateProvider notificationTemplateProvider
    ) {
        super(expected, messageService, requestTemplateProvider, notificationTemplateProvider);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResourceRequestBuilder<T> withPayload(Object payload){
        this.optPayload = Optional.ofNullable(payload);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResourceRequestBuilder<T> throwOnRejection(){
        this.throwOnRejection = true;
        return this;
    }

    /**
     * Set the operation to UPDATE: describes a {@link de.fraunhofer.iais.eis.ResourceUpdateMessage}.
     *
     * @param affectedResource affected resource id for message header
     * @return this builder instance
     */
    public ResourceRequestBuilder<T> operationUpdate(URI affectedResource){
        this.operation = Crud.UPDATE;
        this.affectedResource = affectedResource;
        return this;
    }

    /**
     * Set the operation to DELETE: describes a {@link de.fraunhofer.iais.eis.ResourceUnavailableMessage}.
     *
     * @param affectedResource affected resource id for message header
     * @return this builder instance
     */
    public ResourceRequestBuilder<T> operationDelete(URI affectedResource){
        this.operation = Crud.DELETE;
        this.affectedResource = affectedResource;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MessageContainer<T> execute(URI target)
            throws DapsTokenManagerException,
            ShaclValidatorException,
            SerializeException,
            ClaimsException,
            UnknownResponseException,
            SendMessageException,
            MultipartParseException,
            IOException,
            DeserializeException,
            RejectionException,
            UnexpectedPayloadException {
        switch (protocolType) {
            case IDSCP:
                throw new UnsupportedOperationException("Not yet implemented Protocol!");
            case LDP:
                throw new UnsupportedOperationException("Not yet implemented Protocol!");
            case MULTIPART:
                switch (operation) {
                    case UPDATE:
                        var updateMessage = notificationTemplateProvider
                                .resourceUpdateMessageTemplate(affectedResource).buildMessage();
                        return sendMultipart(target, updateMessage);
                    case DELETE:
                        var deleteMessage = notificationTemplateProvider
                                .resourceUnavailableMessageTemplate(affectedResource).buildMessage();
                        return sendMultipart(target, deleteMessage);
                    default:
                        throw new UnsupportedOperationException("Unsupported Operation!");
                }
            default:
                throw new UnsupportedOperationException("Unsupported Protocol!");
        }
    }

//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public ResourceRequestBuilder<T> useIDSCP() {
//        this.protocolType = ProtocolType.IDSCP;
//        return this;
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public ResourceRequestBuilder<T> useLDP() {
//        this.protocolType = ProtocolType.LDP;
//        return this;
//    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResourceRequestBuilder<T> useMultipart() {
        this.protocolType = ProtocolType.MULTIPART;
        return this;
    }
}
