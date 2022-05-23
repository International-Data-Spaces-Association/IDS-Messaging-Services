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
package ids.messaging.dispatcher.filter;

/**
 * Exception that is thrown when an error occurs during
 * preprocessing of incoming headers with a {@link PreDispatchingFilter}.
 */
public class PreDispatchingFilterException extends Exception {
    private static final long serialVersionUID = 42L;

    /**
     * Exception that is thrown when an error occurs during
     * preprocessing of incoming headers.
     */
    public PreDispatchingFilterException() {
        super();
    }

    /**
     * Exception that is thrown when an error occurs during
     * preprocessing of incoming headers.
     *
     * @param message Message to be thrown.
     */
    public PreDispatchingFilterException(final String message) {
        super(message);
    }

    /**
     * Exception that is thrown when an error occurs during preprocessing of incoming headers.
     *
     * @param message Message to be thrown.
     * @param cause Cause of Exception.
     */
    public PreDispatchingFilterException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Exception that is thrown when an error occurs during preprocessing of incoming headers.
     *
     * @param cause Cause of Exception.
     */
    public PreDispatchingFilterException(final Throwable cause) {
        super(cause);
    }

    protected PreDispatchingFilterException(final String message,
                                            final Throwable cause,
                                            final boolean enableSuppression,
                                            final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
