package de.fraunhofer.ids.messaging.handler.message;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for MessageHandlers that support multiple MessageTypes, collection of SupportedMessageType annotations.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SupportedMessageTypes {
    SupportedMessageType[] value();
}
