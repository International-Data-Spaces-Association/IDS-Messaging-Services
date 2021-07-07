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
 * RequestBuilder for messages with subject 'connector'.
 *
 * @param <T> Type of expected Payload.
 */
public class ConnectorRequestBuilder<T> extends IdsRequestBuilder<T> implements ExecutableBuilder<T>, SupportsAllProtocols<T, ConnectorRequestBuilder<T>> {

    private URI affectedConnector;

    ConnectorRequestBuilder(
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
    public ConnectorRequestBuilder<T> withPayload(Object payload){
        this.optPayload = Optional.ofNullable(payload);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConnectorRequestBuilder<T> throwOnRejection(){
        this.throwOnRejection = true;
        return this;
    }

    /**
     * Set the operation to UPDATE: describes a {@link de.fraunhofer.iais.eis.ConnectorUpdateMessage}.
     *
     * @param affectedConnector affected connector id for message header
     * @return this builder instance
     */
    public ConnectorRequestBuilder<T> operationUpdate(URI affectedConnector){
        this.operation = Crud.UPDATE;
        this.affectedConnector = affectedConnector;
        return this;
    }

    /**
     * Set the operation to DELETE: describes a {@link de.fraunhofer.iais.eis.ConnectorUnavailableMessage}.
     *
     * @param affectedConnector affected connector id for message header
     * @return this builder instance
     */
    public ConnectorRequestBuilder<T> operationDelete(URI affectedConnector){
        this.operation = Crud.DELETE;
        this.affectedConnector = affectedConnector;
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
                                .connectorUpdateMessageTemplate(affectedConnector).buildMessage();
                        return sendMultipart(target, updateMessage);
                    case DELETE:
                        var deleteMessage = notificationTemplateProvider
                                .connectorUnavailableMessageTemplate(affectedConnector).buildMessage();
                        return sendMultipart(target, deleteMessage);
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
    public ConnectorRequestBuilder<T> useIDSCP() {
        this.protocolType = ProtocolType.IDSCP;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConnectorRequestBuilder<T> useLDP() {
        this.protocolType = ProtocolType.LDP;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConnectorRequestBuilder<T> useMultipart() {
        this.protocolType = ProtocolType.MULTIPART;
        return this;
    }
}
