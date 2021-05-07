package de.fraunhofer.ids.framework.appstore;

import java.io.IOException;
import java.net.URI;

import de.fraunhofer.iais.eis.AppRegistrationResponseMessage;
import de.fraunhofer.iais.eis.AppResource;
import de.fraunhofer.ids.framework.daps.ClaimsException;
import de.fraunhofer.ids.framework.daps.ConnectorMissingCertExtensionException;
import de.fraunhofer.ids.framework.daps.DapsConnectionException;
import de.fraunhofer.ids.framework.daps.DapsEmptyResponseException;
import de.fraunhofer.ids.framework.messaging.protocol.multipart.MessageAndPayload;
import de.fraunhofer.ids.framework.messaging.protocol.multipart.mapping.ArtifactResponseMAP;
import de.fraunhofer.ids.framework.messaging.protocol.multipart.mapping.InfrastructurePayloadMAP;
import de.fraunhofer.ids.framework.messaging.protocol.multipart.mapping.ResourceMAP;
import de.fraunhofer.ids.framework.util.MultipartParseException;

/**
 *
 */
public interface IDSAppStoreService {

    /**
     * @param appURI URI of the App Store to be used
     *
     * @return MessageAndPayload Object containing the IDS Message Headers and the Selfdescription of the App Store as the payload
     *
     * @throws ConnectorMissingCertExtensionException
     * @throws DapsConnectionException
     * @throws DapsEmptyResponseException
     * @throws ClaimsException
     * @throws MultipartParseException
     * @throws IOException
     */
    InfrastructurePayloadMAP requestSelfDescription( URI appURI)
            throws
            ConnectorMissingCertExtensionException,
            DapsConnectionException,
            DapsEmptyResponseException,
            ClaimsException,
            MultipartParseException,
            IOException;

    /**
     * @param appStoreURI URI of the App Store to be used
     * @param app  URI of the requested app
     *
     * @return MessageAndPayload Object containing the IDS Message Headers and the Selfdescription of the App as the payload
     *
     * @throws ClaimsException
     * @throws MultipartParseException
     * @throws IOException
     * @throws ConnectorMissingCertExtensionException
     * @throws DapsConnectionException
     * @throws DapsEmptyResponseException
     */
    ResourceMAP requestAppDescription( URI appStoreURI, URI app)
            throws
            ClaimsException,
            MultipartParseException,
            IOException,
            ConnectorMissingCertExtensionException,
            DapsConnectionException,
            DapsEmptyResponseException;

    /**
     * @param appStoreURI URI of the App Store to be used
     * @param app  URI of the requested app
     *
     * @return
     *
     * @throws ConnectorMissingCertExtensionException
     * @throws DapsConnectionException
     * @throws DapsEmptyResponseException
     * @throws ClaimsException
     * @throws MultipartParseException
     * @throws IOException
     */
    ArtifactResponseMAP requestAppArtifact(URI appStoreURI, URI app)
            throws
            ConnectorMissingCertExtensionException,
            DapsConnectionException,
            DapsEmptyResponseException,
            ClaimsException,
            MultipartParseException,
            IOException;
}


