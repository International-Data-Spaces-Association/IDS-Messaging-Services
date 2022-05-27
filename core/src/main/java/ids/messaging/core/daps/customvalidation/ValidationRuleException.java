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
package ids.messaging.core.daps.customvalidation;

/**
 * Exception which is thrown by {@link DatValidationRule} evaluation.
 */
public class ValidationRuleException extends Exception {

    /**
     * Constructor for ValidationRuleException.
     */
    public ValidationRuleException() {
        super();
    }

    /**
     * Constructor for ValidationRuleException.
     *
     * @param message The exception message.
     */
    public ValidationRuleException(final String message) {
        super(message);
    }
}
