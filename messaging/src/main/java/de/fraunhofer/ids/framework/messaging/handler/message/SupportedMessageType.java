package de.fraunhofer.ids.framework.messaging.handler.message;

import de.fraunhofer.iais.eis.Message;

import java.lang.annotation.*;

/**
 * This annotation specifies which Type of RequestMessage can be handled by a specific MessageHandler implementation.
 */
@Target( ElementType.TYPE )
@Retention( RetentionPolicy.RUNTIME )
@Repeatable( value = SupportedMessageTypes.class )
public @interface SupportedMessageType {
    Class<? extends Message> value();
}
