package de.fraunhofer.ids.framework.appstore;

import java.io.IOException;
import java.net.URI;

import de.fraunhofer.ids.messaging.core.daps.ClaimsException;
import de.fraunhofer.ids.messaging.core.daps.DapsTokenManagerException;
import de.fraunhofer.ids.messaging.core.util.MultipartParseException;
import de.fraunhofer.ids.messaging.protocol.multipart.mapping.ArtifactResponseMAP;
import de.fraunhofer.ids.messaging.protocol.multipart.mapping.InfrastructurePayloadMAP;
import de.fraunhofer.ids.messaging.protocol.multipart.mapping.ResourceMAP;


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
    ResourceMAP requestAppDescription( URI appStoreURI, URI app)
            throws
            ClaimsException,
            MultipartParseException,
            IOException,
            DapsTokenManagerException;
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
    InfrastructurePayloadMAP requestAppStoreDescription( URI appStoreURI)
            throws
            ClaimsException,
            MultipartParseException,
            IOException,
            DapsTokenManagerException;
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
    ArtifactResponseMAP requestAppArtifact(URI appStoreURI, URI app)
            throws
            DapsTokenManagerException,
            ClaimsException,
            MultipartParseException,
            IOException;
}


