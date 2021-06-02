package de.fraunhofer.ids.messaging.core.daps.customvalidation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

/**
 * Evaluation result of a custom rule, can be either success or failure
 */
@Getter
@AllArgsConstructor
public class ValidationRuleResult {

    /**
     * Create a successResult
     *
     * @return CustomRuleResult with success flag set to true
     */
    public static ValidationRuleResult success(){
        return new ValidationRuleResult(true, "");
    }

    /**
     * Create a failureResult with an error Message
     *
     * @param message error message (information why validation failed)
     * @return CustomRuleResult with success flag set to false
     */
    public static ValidationRuleResult failure(@NonNull String message){
        return new ValidationRuleResult(false, message);
    }

    private boolean success;

    private String message;

}
