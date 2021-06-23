package de.fraunhofer.ids.messaging.requests;

import de.fraunhofer.iais.eis.RequestMessage;
import de.fraunhofer.ids.messaging.core.daps.DapsTokenManagerException;

@FunctionalInterface
public interface RequestMessageTemplate<T extends RequestMessage> {
    T buildMessage() throws DapsTokenManagerException;
}
