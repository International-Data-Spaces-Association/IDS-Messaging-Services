package de.fraunhofer.ids.messaging.requests;

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.RejectionMessage;
import de.fraunhofer.iais.eis.RejectionReason;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import lombok.Getter;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Container holding ids header fields, the underlying IDS message they were parsed from and the message payload
 *
 * @param <T> Type of payload
 */
@Getter
public class MessageContainer<T> {

    private static final Serializer SERIALIZER = new Serializer();

    private final HeaderContainer headers;
    private final T payload;
    private final Message underlyingMessage;
    private final Optional<RejectionReason> rejectionReason;

    public MessageContainer(Message underlyingMessage, final T payload){
        this.payload = payload;
        this.underlyingMessage = underlyingMessage;
        this.headers = containerFromMessage(underlyingMessage);
        rejectionReason = underlyingMessage instanceof RejectionMessage ?
                    Optional.of(((RejectionMessage) underlyingMessage).getRejectionReason()) :
                    Optional.empty();
    }

    public MessageContainer(Map<String,String> headers, final T payload){
        //TODO build headerContainer from httpHeaders given in header map (used for incoming IDS_LDP messages)
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    private boolean isRejection(){
        return rejectionReason.isPresent();
    }

    private URI getMessageID(){
        return underlyingMessage.getId();
    }

    private HeaderContainer containerFromMessage(Message message){
        return new HeaderContainer(
                Optional.ofNullable(message.getAuthorizationToken()),
                message.getSecurityToken(),
                message.getIssued(),
                message.getCorrelationMessage(),
                Optional.ofNullable(message.getContentVersion()),
                message.getIssuerConnector(),
                message.getSenderAgent(),
                message.getRecipientAgent() != null ? message.getRecipientAgent() : List.of(),
                Optional.ofNullable(message.getTransferContract()),
                message.getContentVersion()
        );
    }
}
