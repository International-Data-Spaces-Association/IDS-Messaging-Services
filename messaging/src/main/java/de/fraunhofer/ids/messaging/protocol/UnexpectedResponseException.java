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
package de.fraunhofer.ids.messaging.protocol;

import de.fraunhofer.ids.messaging.protocol.multipart.MessageAndPayload;
import okhttp3.Response;

/**
 * An exception that is thrown after converting a {@link Response} into the corresponding {@link MessageAndPayload object} if
 * the received response-type is not expected as a response to the request send.
 */
public class UnexpectedResponseException extends Exception {
    private static final long serialVersionUID = 42L;
    /**
     *  An exception that is thrown after converting a {@link Response} into the corresponding {@link MessageAndPayload object} if
     *  the received response-type is not expected as a response to the request send.
     *
     * @param message Message of the Exception to be thrown
     */
    public UnexpectedResponseException(final String message) {
        super(message);
    }
}
