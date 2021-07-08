package de.fraunhofer.ids.messaging.requests.exceptions;

import de.fraunhofer.ids.messaging.requests.MessageContainer;
import lombok.Getter;

@Getter
public class UnexpectedPayloadException extends IdsRequestException{

    private final MessageContainer<?> messageContainer;

    public UnexpectedPayloadException(MessageContainer<?> messageContainer){super();
        this.messageContainer = messageContainer;
    }

    public UnexpectedPayloadException(String message, MessageContainer<?> messageContainer){super(message);
        this.messageContainer = messageContainer;
    }
}
