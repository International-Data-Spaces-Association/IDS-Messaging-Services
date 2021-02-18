package de.fraunhofer.ids.framework.messaging.handler.message;

/**
 * An exception that is thrown during message-handling of a MessageHandler
 */
public class MessageHandlerException extends Exception {
    /**
     * An exception that is thrown during message-handling of a MessageHandler
     */
    public MessageHandlerException() {
        super();
    }

    /**
     * An exception that is thrown during message-handling of a MessageHandler
     * @param message Message of the Exception to be thrown
     */
    public MessageHandlerException( final String message ) {
        super(message);
    }

    /**
     * An exception that is thrown during message-handling of a MessageHandler
     * @param message Message of the Exception to be thrown
     * @param cause Throwable cause of the Exception
     */
    public MessageHandlerException( final String message, final Throwable cause ) {
        super(message, cause);
    }

    /**
     * An exception that is thrown during message-handling of a MessageHandler
     * @param cause Throwable cause of the Exception
     */
    public MessageHandlerException( final Throwable cause ) {
        super(cause);
    }

    /**
     * An exception that is thrown during message-handling of a MessageHandler
     * @param message Message of the Exception to be thrown
     * @param cause Throwable cause of the Exception
     * @param enableSuppression Enable Suppression?
     * @param writableStackTrace Write to Stacktrace?
     */
    public MessageHandlerException( final String message, final Throwable cause, final boolean enableSuppression,
                                    final boolean writableStackTrace ) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
