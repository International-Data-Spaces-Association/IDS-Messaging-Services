package de.fraunhofer.ids.framework.appstore;

import java.io.IOException;
import java.net.URI;

import de.fraunhofer.iais.eis.AppResource;
import de.fraunhofer.iais.eis.AppStore;
import de.fraunhofer.iais.eis.RejectionMessage;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.ids.messaging.core.config.ConfigContainer;
import de.fraunhofer.ids.messaging.core.daps.*;
import de.fraunhofer.ids.messaging.core.util.MultipartParseException;
import de.fraunhofer.ids.messaging.protocol.InfrastructureService;
import de.fraunhofer.ids.messaging.protocol.MessageService;
import de.fraunhofer.ids.messaging.protocol.multipart.MessageAndPayload;
import de.fraunhofer.ids.messaging.protocol.multipart.mapping.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Generates  ids multipart messages and sends them to the App Store
 */
@Slf4j
@Service
public class AppStoreService extends InfrastructureService
        implements IDSAppStoreService {


    /**
     * Creates the IDSAppStore Communication controller.
     *
     * @param container      Configuration container
     * @param tokenProvider  providing DAT Token for RequestMessage
     * @param messageService providing Messaging functionality
     */
    public AppStoreService(
            final ConfigContainer container,
            final DapsTokenProvider tokenProvider,
            final MessageService messageService
    ) {
        super(container, tokenProvider, messageService);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InfrastructurePayloadMAP requestAppStoreDescription( final URI appStoreURI )
            throws
            ClaimsException,
            MultipartParseException,
            IOException,
            ConnectorMissingCertExtensionException,
            DapsConnectionException,
            DapsEmptyResponseException {


        final var header = MessageBuilder
                .buildAppDescriptionRequestMessage(
                        tokenProvider.getDAT(), container.getConnector(),null);

        final var messageAndPayload = new GenericMessageAndPayload(header);

        final var map = super.expectDescriptionResponseMAP(messageService.sendIdsMessage(messageAndPayload, appStoreURI));
        if( map.getPayload().isPresent() ) {
            return new InfrastructurePayloadMAP(map.getMessage(),
                                   new Serializer().deserialize(map.getPayload().get(), AppStore.class));
        }else {
            throw new IOException("Description request message did not return a response with payload.");
        }

    }

    @Override
    public ResourceMAP requestAppDescription( final URI appStoreURI, final URI app )
            throws
            ClaimsException,
            MultipartParseException,
            IOException,
            ConnectorMissingCertExtensionException,
            DapsConnectionException,
            DapsEmptyResponseException {


        final var header = MessageBuilder
                .buildAppDescriptionRequestMessage( tokenProvider.getDAT(), container.getConnector(), app);

        final var messageAndPayload = new GenericMessageAndPayload(header);

        final var map = super.expectDescriptionResponseMAP(messageService.sendIdsMessage(messageAndPayload, appStoreURI));
        if( map.getPayload().isPresent() ) {

            return new ResourceMAP(map.getMessage(),
                                   new Serializer().deserialize(map.getPayload().get(), AppResource.class));
        }else {
            throw new IOException("Description request message did not return a response with payload.");
        }

    }

    @Override
    public ArtifactResponseMAP requestAppArtifact( final URI appStoreURI, final URI app )
            throws
            ConnectorMissingCertExtensionException,
            DapsConnectionException,
            DapsEmptyResponseException,
            ClaimsException,
            MultipartParseException,
            IOException {

        final var header = MessageBuilder.buildAppArtifactRequestMessage(tokenProvider.getDAT(), container.getConnector(), app);
        final var messageAndPayload = new GenericMessageAndPayload(header);

        return expectArtifactResponseMAP(messageService.sendIdsMessage(messageAndPayload, appStoreURI));

    }

    private ArtifactResponseMAP expectArtifactResponseMAP( final MessageAndPayload<?, ?> response )
            throws IOException {

        if( response instanceof ArtifactResponseMAP ) {
            return (ArtifactResponseMAP) response;

        }
        if( response instanceof RejectionMAP ) {
            final var  rejectionMessage = (RejectionMessage) response.getMessage();
            throw new IOException(
                    "Message rejected by target with following Reason: " + rejectionMessage.getRejectionReason());
        }
        throw new IOException(String.format("Unexpected Message of type %s was returned",
                                            response.getMessage().getClass().toString()));
    }
}
