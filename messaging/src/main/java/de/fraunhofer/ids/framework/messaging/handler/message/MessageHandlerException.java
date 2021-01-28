package de.fraunhofer.ids.framework.messaging.handler.message;

/**
 * An exception that is thrown during message-handling of a MessageHandler
 */
public class MessageHandlerException extends Exception {
    public MessageHandlerException() {
        super();
    }

    public MessageHandlerException( final String message ) {
        super(message);
    }

    public MessageHandlerException( final String message, final Throwable cause ) {
        super(message, cause);
    }

    public MessageHandlerException( final Throwable cause ) {
        super(cause);
    }

    public MessageHandlerException( final String message, final Throwable cause, final boolean enableSuppression,
                                    final boolean writableStackTrace ) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
