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
 * RequestBuilder for messages with subject 'description'.
 *
 * @param <T> Type of expected Payload.
 */
public class DescriptionRequestBuilder<T> extends IdsRequestBuilder<T> implements ExecutableBuilder<T>, SupportsAllProtocols<T, DescriptionRequestBuilder<T>> {

    private URI requestedElement;

    DescriptionRequestBuilder(
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
    public DescriptionRequestBuilder<T> withPayload(Object payload){
        this.optPayload = Optional.ofNullable(payload);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DescriptionRequestBuilder<T> throwOnRejection(){
        this.throwOnRejection = true;
        return this;
    }

    /**
     * Set the operation to RECEIVE: describes a {@link de.fraunhofer.iais.eis.DescriptionRequestMessage}.
     *
     * @param requestedElement requested element id for message header (null => selfdescription)
     * @return this builder instance
     */
    public DescriptionRequestBuilder<T> operationGet(URI requestedElement){
        this.operation = Crud.RECEIVE;
        this.requestedElement = requestedElement;
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
                switch (operation){
                    case RECEIVE:
                        //build and send artifact request message
                        var message = requestTemplateProvider.descriptionRequestMessageTemplate(requestedElement)
                                .buildMessage();
                        return sendMultipart(target, message);
                    default:
                        throw new UnsupportedOperationException("Unsupported Operation!");
                }
            default:
                throw new UnsupportedOperationException("Unsupported Protocol!");
        }
    }

    @Override
    public DescriptionRequestBuilder<T> useIDSCP() {
        this.protocolType = ProtocolType.IDSCP;
        return this;
    }

    @Override
    public DescriptionRequestBuilder<T> useLDP() {
        this.protocolType = ProtocolType.LDP;
        return this;
    }

    @Override
    public DescriptionRequestBuilder<T> useMultipart() {
        this.protocolType = ProtocolType.MULTIPART;
        return this;
    }

}
