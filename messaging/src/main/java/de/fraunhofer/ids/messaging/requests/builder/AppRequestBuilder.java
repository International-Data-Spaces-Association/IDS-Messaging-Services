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
 * RequestBuilder for messages with subject 'app'.
 *
 * @param <T> Type of expected Payload.
 */
public class AppRequestBuilder<T> extends IdsRequestBuilder<T> implements ExecutableBuilder<T>, SupportsAllProtocols<T, AppRequestBuilder<T>> {

    private URI affectedApp;

    AppRequestBuilder(
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
    public AppRequestBuilder<T> withPayload(final Object payload) {
        this.optPayload = Optional.ofNullable(payload);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AppRequestBuilder<T> throwOnRejection() {
        this.throwOnRejection = true;
        return this;
    }

    /**
     * Set the operation to UPDATE: describes an {@link de.fraunhofer.iais.eis.AppAvailableMessage}.
     *
     * @param affectedApp affected app id for message header
     * @return this builder instance
     */
    public AppRequestBuilder<T> operationCreate(URI affectedApp) {
        operation = Crud.UPDATE;
        this.affectedApp = affectedApp;
        return this;
    }

    /**
     * Set the operation to DELETE: describes an {@link de.fraunhofer.iais.eis.AppDeleteMessage}.
     *
     * @param affectedApp affected app id for message header
     * @return this builder instance
     */
    public AppRequestBuilder<T> operationDelete(URI affectedApp) {
        operation = Crud.DELETE;
        this.affectedApp = affectedApp;
        return this;
    }

    /**
     * Set the operation to DISABLE: describes an {@link de.fraunhofer.iais.eis.AppUnavailableMessage}.
     *
     * @param affectedApp affected app id for message header
     * @return this builder instance
     */
    public AppRequestBuilder<T> operationUnavailable(URI affectedApp) {
        operation = Crud.DISABLE;
        this.affectedApp = affectedApp;
        return this;
    }

    /**
     * Set the operation to REGISTER: describes an {@link de.fraunhofer.iais.eis.AppRegistrationRequestMessage}.
     *
     * @param affectedApp affected app id for message header
     * @return this builder instance
     */
    public AppRequestBuilder<T> operationRegistration(URI affectedApp) {
        operation = Crud.REGISTER;
        this.affectedApp = affectedApp;
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
                                .appAvailableMessageTemplate(affectedApp).buildMessage();
                        return sendMultipart(target, updateMessage);
                    case DELETE:
                        var deleteMessage = notificationTemplateProvider
                                .appDeleteMessageTemplate(affectedApp).buildMessage();
                        return sendMultipart(target, deleteMessage);
                    case DISABLE:
                        var disableMessage = notificationTemplateProvider
                                .appUnavailableMessageTemplate(affectedApp).buildMessage();
                        return sendMultipart(target, disableMessage);
                    case REGISTER:
                        var registerMessage = requestTemplateProvider
                                .appRegistrationRequestMessageTemplate(affectedApp).buildMessage();
                        return sendMultipart(target, registerMessage);
                    default:
                        throw new UnsupportedOperationException("Unsupported Operation!");
                }
            default:
                throw new UnsupportedOperationException("Unsupported Protocol!");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AppRequestBuilder<T> useIDSCP() {
        this.protocolType = ProtocolType.IDSCP;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AppRequestBuilder<T> useLDP() {
        this.protocolType = ProtocolType.LDP;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AppRequestBuilder<T> useMultipart() {
        this.protocolType = ProtocolType.MULTIPART;
        return this;
    }
}
