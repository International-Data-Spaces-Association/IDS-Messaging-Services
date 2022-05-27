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

import io.jsonwebtoken.Claims;

/**
 * Interface for adding custom DAPS DAT validation rules.
 */
@FunctionalInterface
public interface DatValidationRule {
    /**
     * Adds the possibility to add custom validation rules.
     *
     * @param toVerify The Claims to verify.
     * @return ValidationRuleResult if successful or not and message.
     * @throws ValidationRuleException If exception thrown by custom check.
     */
    ValidationRuleResult checkRule(Claims toVerify) throws ValidationRuleException;
}
