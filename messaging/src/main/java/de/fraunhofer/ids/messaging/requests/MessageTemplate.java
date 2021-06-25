package de.fraunhofer.ids.messaging.requests;

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.RequestMessage;
import de.fraunhofer.ids.messaging.core.daps.DapsTokenManagerException;

@FunctionalInterface
public interface MessageTemplate<T extends Message> {

    /**
     * Build the message defined by this template.
     *
     * @return built message by template
     * @throws DapsTokenManagerException when no DAT for Message can be received
     */
    T buildMessage() throws DapsTokenManagerException;
}
