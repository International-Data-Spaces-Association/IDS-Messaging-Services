package de.fraunhofer.ids.framework.util;

public class MultipartParseException extends Exception {

    public MultipartParseException(final String message){
        super(message);
    }
    public MultipartParseException(final String message, final Exception cause){
        super(message, cause);
    }
}
