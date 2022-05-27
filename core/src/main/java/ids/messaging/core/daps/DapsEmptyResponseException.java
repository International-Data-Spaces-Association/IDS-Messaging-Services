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
package ids.messaging.core.daps;

/**
 * Exception which is thrown when the Response from the DAPS is empty.
 */
public class DapsEmptyResponseException extends DapsTokenManagerException {
    private static final long serialVersionUID = 42L;

    /**
     * For Throwing a EmptyDapsResponseException with a custom error message.
     *
     * @param message The error message to be included with the exception.
     */
    public DapsEmptyResponseException(final String message) {
        super(message);
    }

}
