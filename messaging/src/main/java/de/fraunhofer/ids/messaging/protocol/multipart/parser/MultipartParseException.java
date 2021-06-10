package de.fraunhofer.ids.messaging.protocol.multipart.parser;

public class MultipartParseException extends Exception {
    private static final long serialVersionUID = 42L;

    public MultipartParseException(final String message) {
        super(message);
    }

    public MultipartParseException(final String message, final Exception cause) {
        super(message, cause);
    }
}
