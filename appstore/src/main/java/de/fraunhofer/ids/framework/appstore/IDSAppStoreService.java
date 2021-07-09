package de.fraunhofer.ids.framework.appstore;

import java.io.IOException;
import java.net.URI;

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


/**
 *
 */
public interface IDSAppStoreService {


    /**
     * @param appStoreURI URI of the App Store to be used
     * @param app  URI of the requested app
     *
     * @return Response MAP with the SelfDescription in the payload as an AppStore
     *
     * @throws MultipartParseException if response could not be parsed to header and payload.@throws ClaimsException
     * @throws IOException if message could not be sent or Serializer could not parse RDF to Java Object.
     * @throws DapsTokenManagerException if no DAT for sending the message could be received.
     * @throws ClaimsException if DAT of incoming message could not be validated.
     */
    MessageContainer<Object> requestAppDescription( URI appStoreURI, URI app)
            throws
            ClaimsException,
            MultipartParseException,
            IOException,
            DapsTokenManagerException, ShaclValidatorException,
            SerializeException, UnknownResponseException, SendMessageException,
            DeserializeException, RejectionException,
            UnexpectedPayloadException;
    /**
     * @param appStoreURI URI of the App Store to be used
     *
     * @return Response MAP with the SelfDescription in the payload as AppResource
     *
     * @throws MultipartParseException if response could not be parsed to header and payload.@throws ClaimsException
     * @throws IOException if message could not be sent or Serializer could not parse RDF to Java Object.
     * @throws DapsTokenManagerException if no DAT for sending the message could be received.
     * @throws ClaimsException if DAT of incoming message could not be validated.
     */
    MessageContainer<Object> requestAppStoreDescription( URI appStoreURI)
            throws
            ClaimsException,
            IOException,
            DapsTokenManagerException, MultipartParseException,
            ShaclValidatorException, SerializeException,
            UnknownResponseException, SendMessageException,
            DeserializeException, RejectionException,
            UnexpectedPayloadException;
    /**
     * @param appStoreURI URI of the App Store to be used
     * @param app  URI of the requested app
     *
     * @return Response MAP with the SelfDescription in the payload as String
     *
     * @throws MultipartParseException if response could not be parsed to header and payload.@throws ClaimsException
     * @throws IOException if message could not be sent or Serializer could not parse RDF to Java Object.
     * @throws DapsTokenManagerException if no DAT for sending the message could be received.
     * @throws ClaimsException if DAT of incoming message could not be validated.
     */
    MessageContainer<Object> requestAppArtifact( URI appStoreURI, URI app)
            throws
            DapsTokenManagerException,
            ClaimsException,
            MultipartParseException,
            IOException, ShaclValidatorException, SerializeException,
            UnknownResponseException, SendMessageException,
            DeserializeException, RejectionException,
            UnexpectedPayloadException;
}


