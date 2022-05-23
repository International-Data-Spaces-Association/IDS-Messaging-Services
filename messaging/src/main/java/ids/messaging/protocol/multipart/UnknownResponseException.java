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
package ids.messaging.protocol.multipart;

/**
 * An exception that is thrown during converting a Response into the
 * corresponding MessageAndPayload if no possible cast found.
 */
public class UnknownResponseException extends Exception {
    private static final long serialVersionUID = 42L;
    /**
     * An exception that is thrown during converting a Response into the
     * corresponding MessageAndPayload if no possible cast found.
     *
     * @param message Message of the Exception to be thrown.
     */
    public UnknownResponseException(final String message) {
        super(message);
    }
}
