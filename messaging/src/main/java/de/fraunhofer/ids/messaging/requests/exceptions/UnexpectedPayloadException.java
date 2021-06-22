package de.fraunhofer.ids.messaging.requests.exceptions;

public class UnexpectedPayloadException extends IdsRequestException{

    public UnexpectedPayloadException(){super();}

    public UnexpectedPayloadException(String message){super(message);}
}
