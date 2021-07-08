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

/**
 * A RequestBuilder that is able to execute a request will implement this class.
 *
 * @param <T> type of expected payload
 */
public interface ExecutableBuilder<T> {

    /**
     * Send the message using the current information of the builder.
     *
     * @param target targetURI message will be sent to
     * @return MessageContainer containing response
     * @throws DapsTokenManagerException when DAT cannot be received from DAPS
     * @throws ShaclValidatorException when Shacl Validation fails
     * @throws SerializeException when the payload cannot be serialized
     * @throws ClaimsException when DAT of response is not valid
     * @throws UnknownResponseException when type of response is not known
     * @throws SendMessageException when an IOException is thrown by the httpclient when sending the message
     * @throws MultipartParseException when the response cannot be parsed as multipart
     * @throws IOException when some other error happens while sending the message
     * @throws DeserializeException when response cannot be deserialized
     * @throws RejectionException when response is a RejectionMessage (and 'throwOnRejection' is set in the builder)
     * @throws UnexpectedPayloadException when payload is not of type T
     */
    MessageContainer<T> execute(URI target)
            throws DapsTokenManagerException,
            ShaclValidatorException,
            SerializeException,
            ClaimsException,
            UnknownResponseException,
            SendMessageException,
            MultipartParseException,
            IOException,
            DeserializeException,
            RejectionException,
            UnexpectedPayloadException;

}
