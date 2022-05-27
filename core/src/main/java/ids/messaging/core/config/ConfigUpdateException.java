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
 * Exception that is thrown, when an error occurs while trying to change the configuration
 * using {@link ConfigContainer}.
 */
public class ConfigUpdateException extends Exception {
    private static final long serialVersionUID = 42L;

    /**
     * Create a ConfigurationUpdateException with a given Message and Cause.
     *
     * @param message Error message of the exception.
     * @param cause Cause for the exception.
     */
    public ConfigUpdateException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
