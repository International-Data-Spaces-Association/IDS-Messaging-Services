package de.fraunhofer.ids.messaging.core.daps.customvalidation;

/**
 * Exception which is thrown by {@link DatValidationRule} evaluation
 */
public class ValidationRuleException extends Exception{

    public ValidationRuleException(){super();}

    public ValidationRuleException(String message){super(message);}
}
