package de.fraunhofer.ids.messaging.core.daps.customcheck;

/**
 * Exception which is thrown by {@link CustomDapsRule} evaluation
 */
public class CustomDapsRuleException extends Exception{

    public CustomDapsRuleException(){super();}

    public CustomDapsRuleException(String message){super(message);}
}
