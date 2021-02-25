package de.fraunhofer.ids.framework.messaging.handler.message;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import de.fraunhofer.iais.eis.Message;

/**
 * This annotation specifies which Type of RequestMessage can be handled by a specific MessageHandler implementation.
 */
@Target( ElementType.TYPE )
@Retention( RetentionPolicy.RUNTIME )
@Repeatable( value = SupportedMessageTypes.class )
public @interface SupportedMessageType {
    Class<? extends Message> value();
}
