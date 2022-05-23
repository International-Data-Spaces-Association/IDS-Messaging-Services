/*
 * Copyright Fraunhofer Institute for Software and Systems Engineering
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *  Contributors:
 *       sovity GmbH
 *
 */
package ids.messaging.handler.message;

/**
 * An exception that is thrown during message-handling of a MessageHandler.
 */
public class MessageHandlerException extends Exception {
    private static final long serialVersionUID = 42L;

    /**
     * An exception that is thrown during message-handling of a MessageHandler.
     */
    public MessageHandlerException() {
        super();
    }

    /**
     * An exception that is thrown during message-handling of a MessageHandler.
     *
     * @param message Message of the Exception to be thrown.
     */
    public MessageHandlerException(final String message) {
        super(message);
    }

    /**
     * An exception that is thrown during message-handling of a MessageHandler.
     *
     * @param message Message of the Exception to be thrown.
     * @param cause Throwable cause of the Exception.
     */
    public MessageHandlerException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * An exception that is thrown during message-handling of a MessageHandler.
     *
     * @param cause Throwable cause of the Exception.
     */
    public MessageHandlerException(final Throwable cause) {
        super(cause);
    }

    /**
     * An exception that is thrown during message-handling of a MessageHandler.
     *
     * @param message Message of the Exception to be thrown.
     * @param cause Throwable cause of the Exception.
     * @param enableSuppression  Enable Suppression?
     * @param writableStackTrace Write to Stacktrace?
     */
    public MessageHandlerException(final String message,
                                   final Throwable cause,
                                   final boolean enableSuppression,
                                   final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
