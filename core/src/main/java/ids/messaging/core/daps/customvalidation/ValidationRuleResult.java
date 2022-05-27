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

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

/**
 * Evaluation result of a custom rule, can be either success or failure.
 */
@Getter
@AllArgsConstructor
public class ValidationRuleResult {

    /**
     * True if validation was successful.
     */
    private boolean success;

    /**
     * The ValidationRuleResult message.
     */
    private String message;

    /**
     * Create a successResult.
     *
     * @return CustomRuleResult with success flag set to true.
     */
    public static ValidationRuleResult success() {
        return new ValidationRuleResult(true, "");
    }

    /**
     * Create a failureResult with an error Message.
     *
     * @param message Error message (information why validation failed).
     * @return CustomRuleResult with success flag set to false.
     */
    public static ValidationRuleResult failure(@NonNull final String message) {
        return new ValidationRuleResult(false, message);
    }
}
