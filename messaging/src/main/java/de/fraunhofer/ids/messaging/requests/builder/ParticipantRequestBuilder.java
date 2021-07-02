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

public class ParticipantRequestBuilder<T> extends IdsRequestBuilder<T> implements ExecutableBuilder<T> {

    private URI affectedParticipant;
    private Crud operation;

    ParticipantRequestBuilder(Class<T> expected, ProtocolType protocolType, MessageService messageService, RequestTemplateProvider requestTemplateProvider, NotificationTemplateProvider notificationTemplateProvider) {
        super(expected, protocolType, messageService, requestTemplateProvider, notificationTemplateProvider);
    }

    @Override
    public ParticipantRequestBuilder<T> withPayload(Object payload){
        this.optPayload = Optional.ofNullable(payload);
        return this;
    }

    @Override
    public ParticipantRequestBuilder<T> throwOnRejection(){
        this.throwOnRejection = true;
        return this;
    }

    public ParticipantRequestBuilder<T> operationUpdate(URI affectedParticipant){
        this.operation = Crud.UPDATE;
        this.affectedParticipant = affectedParticipant;
        return this;
    }

    public ParticipantRequestBuilder<T> operationDelete(URI affectedParticipant){
        this.operation = Crud.DELETE;
        this.affectedParticipant = affectedParticipant;
        return this;
    }

    @Override
    public MessageContainer<T> execute(URI target) throws DapsTokenManagerException, ShaclValidatorException, SerializeException, ClaimsException, UnknownResponseException, SendMessageException, MultipartParseException, IOException, DeserializeException, RejectionException, UnexpectedPayloadException {
        switch (protocolType) {
            case IDSCP:
                throw new UnsupportedOperationException("Not yet implemented Protocol!");
            case LDP:
                throw new UnsupportedOperationException("Not yet implemented Protocol!");
            case MULTIPART:
                switch (operation) {
                    case UPDATE:
                        var updateMessage = notificationTemplateProvider
                                .participantUpdateMessageTemplate(affectedParticipant).buildMessage();
                        return sendMultipart(target, updateMessage);
                    case DELETE:
                        var deleteMessage = notificationTemplateProvider
                                .participantUnavailableMessageTemplate(affectedParticipant).buildMessage();
                        return sendMultipart(target, deleteMessage);
                    default:
                        throw new UnsupportedOperationException("Unsupported Operation!");
                }
            default:
                throw new UnsupportedOperationException("Unsupported Protocol!");
        }
    }
}
