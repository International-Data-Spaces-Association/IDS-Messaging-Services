package de.fraunhofer.ids.messaging.requests;

import de.fraunhofer.iais.eis.RequestMessage;
import de.fraunhofer.ids.messaging.core.daps.DapsTokenManagerException;

@FunctionalInterface
public interface RequestMessageTemplate<T extends RequestMessage> {

    /**
     * Build the message defined by this template.
     *
     * @return built message by template
     * @throws DapsTokenManagerException when no DAT for Message can be received
     */
    T buildMessage() throws DapsTokenManagerException;
}
