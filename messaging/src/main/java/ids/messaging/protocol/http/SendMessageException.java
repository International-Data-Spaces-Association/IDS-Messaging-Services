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
package ids.messaging.protocol.http;

/**
 * If sending the IDS-Request returns an IOException.
 */
public class SendMessageException extends Exception {
    private static final long serialVersionUID = 42L;

    /**
     * If sending the IDS-Request returns an IOException.
     *
     * @param cause Throwable cause of the Exception.
     */
    public SendMessageException(final Throwable cause) {
        super(cause);
    }

    /**
     * If request cannot be sent by builder.
     *
     * @param message Error message for the exception.
     */
    public SendMessageException(final String message) {
        super(message);
    }
}
