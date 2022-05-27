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
package ids.messaging.core.config;

/**
 * Exception thrown on failure by ConfigProducerInterceptors.
 */
public class ConfigProducerInterceptorException extends Exception {

    /**
     * Constructor forConfigProducerInterceptorException.
     */
    public ConfigProducerInterceptorException() {
    }

    /**
     * Exception thrown on failure by ConfigProducerInterceptors.
     *
     * @param message The exception message.
     */
    public ConfigProducerInterceptorException(final String message) {
        super(message);
    }

    /**
     * Exception thrown on failure by ConfigProducerInterceptors.
     *
     * @param message The exception message.
     * @param cause The exception cause.
     */
    public ConfigProducerInterceptorException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Exception thrown on failure by ConfigProducerInterceptors.
     *
     * @param cause The exception cause.
     */
    public ConfigProducerInterceptorException(final Throwable cause) {
        super(cause);
    }

    /**
     * Exception thrown on failure by ConfigProducerInterceptors.
     *
     * @param message The exception message.
     * @param cause The exception cause.
     * @param enableSuppression Whether or not suppression is enabled or disabled.
     * @param writableStackTrace Whether or not the stack trace should be writable.
     */
    public ConfigProducerInterceptorException(final String message,
                                              final Throwable cause,
                                              final boolean enableSuppression,
                                              final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
