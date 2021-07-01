package de.fraunhofer.ids.messaging.requests.builder;

import de.fraunhofer.ids.messaging.common.DeserializeException;
import de.fraunhofer.ids.messaging.common.SerializeException;
import de.fraunhofer.ids.messaging.core.daps.ClaimsException;
import de.fraunhofer.ids.messaging.core.daps.DapsTokenManagerException;
import de.fraunhofer.ids.messaging.protocol.http.SendMessageException;
import de.fraunhofer.ids.messaging.protocol.http.ShaclValidatorException;
import de.fraunhofer.ids.messaging.protocol.multipart.UnknownResponseException;
import de.fraunhofer.ids.messaging.protocol.multipart.parser.MultipartParseException;
import de.fraunhofer.ids.messaging.requests.MessageContainer;
import de.fraunhofer.ids.messaging.requests.exceptions.RejectionException;
import de.fraunhofer.ids.messaging.requests.exceptions.UnexpectedPayloadException;

import java.io.IOException;
import java.net.URI;

public interface ExecutableBuilder<T> {

    MessageContainer<T> execute(URI target)
            throws DapsTokenManagerException,
            ShaclValidatorException,
            SerializeException,
            ClaimsException,
            UnknownResponseException,
            SendMessageException,
            MultipartParseException,
            IOException,
            DeserializeException, RejectionException, UnexpectedPayloadException;

}
