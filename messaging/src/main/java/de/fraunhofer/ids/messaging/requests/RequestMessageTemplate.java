package de.fraunhofer.ids.messaging.requests;

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.RequestMessage;
import de.fraunhofer.ids.messaging.core.daps.ConnectorMissingCertExtensionException;
import de.fraunhofer.ids.messaging.core.daps.DapsConnectionException;
import de.fraunhofer.ids.messaging.core.daps.DapsEmptyResponseException;
import de.fraunhofer.ids.messaging.core.daps.DapsTokenManagerException;

@FunctionalInterface
public interface RequestMessageTemplate<T extends RequestMessage> {
    T buildMessage() throws DapsTokenManagerException;
}
