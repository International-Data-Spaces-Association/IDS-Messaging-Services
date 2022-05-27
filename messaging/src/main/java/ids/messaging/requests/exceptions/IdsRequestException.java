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
package ids.messaging.requests.exceptions;

/**
 * Exception for various cases e.g. no template found.
 */
public class IdsRequestException extends Exception {

    /**
     * Constructor for IdsRequestException without exception message and cause.
     */
    public IdsRequestException() { }

    /**
     * Constructor for IdsRequestException with message.
     *
     * @param message The exception message.
     */
    public IdsRequestException(final String message) {
        super(message);
    }
}
