package de.fraunhofer.ids.messaging.core.daps.customcheck;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

/**
 * Evaluation result of a custom rule, can be either success or failure
 */
@Getter
@AllArgsConstructor
public class CustomRuleResult {

    /**
     * Create a successResult
     *
     * @return CustomRuleResult with success flag set to true
     */
    public static CustomRuleResult success(){
        return new CustomRuleResult(true, "");
    }

    /**
     * Create a failureResult with an error Message
     *
     * @param message error message (information why validation failed)
     * @return CustomRuleResult with success flag set to false
     */
    public static CustomRuleResult failure(@NonNull String message){
        return new CustomRuleResult(false, message);
    }

    private boolean success;

    private String message;

}
