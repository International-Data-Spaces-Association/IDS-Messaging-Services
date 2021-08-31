/*
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
 */
package de.fraunhofer.ids.messaging.core.config.ssl.keystore;

/**
 * Exception which is thrown, when the {@link KeyStoreManager} cannot be initialized.
 */
public class KeyStoreManagerInitializationException extends Exception {
    private static final long serialVersionUID = 42L;

    /**
     * Create a KeyStoreManagerInitializationException with a given Message.
     *
     * @param message Error message of the exception.
     */
    public KeyStoreManagerInitializationException(final String message) {
        super(message);
    }

    /**
     * Create a KeyStoreManagerInitializationException with a given Message and Cause.
     *
     * @param message Error message of the exception.
     * @param cause Cause for the exception.
     */
    public KeyStoreManagerInitializationException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
