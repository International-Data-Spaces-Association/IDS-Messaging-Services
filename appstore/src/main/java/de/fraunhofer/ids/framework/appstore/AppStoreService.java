package de.fraunhofer.ids.framework.appstore;

import java.io.IOException;
import java.net.URI;

import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.ids.framework.config.ConfigContainer;
import de.fraunhofer.ids.framework.daps.*;
import de.fraunhofer.ids.framework.messaging.protocol.MessageService;
import de.fraunhofer.ids.framework.messaging.protocol.multipart.MessageAndPayload;
import de.fraunhofer.ids.framework.messaging.protocol.multipart.mapping.*;
import de.fraunhofer.ids.framework.util.MultipartParseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * App Store Communication Controller. Generates  ids multipart messages and sends them to the App Store API
 */
@Slf4j
@Service
public class AppStoreService implements IDSAppStoreService {

    private final ConfigContainer   container;
    private final DapsTokenProvider tokenProvider;
    private final MessageService    messageService;

    /**
     * Creates the IDSAppStore Communication controller.
     *
     * @param container      Configuration container
     * @param tokenProvider  providing DAT Token for RequestMessage
     * @param messageService providing Messaging functionality
     */
    public AppStoreService( final ConfigContainer container,
                            final DapsTokenProvider tokenProvider,
                            final MessageService messageService ) {
        this.container = container;
        this.tokenProvider = tokenProvider;
        this.messageService = messageService;
    }

    @Override
    public InfrastructurePayloadMAP requestSelfDescription( final URI appURI )
            throws
            ConnectorMissingCertExtensionException,
            DapsConnectionException,
            DapsEmptyResponseException,
            ClaimsException,
            MultipartParseException,
            IOException {

        final var header = MessageBuilder
                .buildDescriptionRequestMessage(tokenProvider.getDAT(), container.getConnector());

        final GenericMessageAndPayload messageAndPayload = new GenericMessageAndPayload(header);

        return expectAppStoreDescriptionResponseMAP(messageService.sendIdsMessage(messageAndPayload, appURI));
    }

    private InfrastructurePayloadMAP expectAppStoreDescriptionResponseMAP( final MessageAndPayload<?, ?> response )
            throws IOException {
        final var map = expectDescriptionResponseMAP(response);
        if( map.getPayload().isPresent() ) {

            return new InfrastructurePayloadMAP(map.getMessage(), new Serializer().deserialize(map.getPayload().get(),
                                                                                               AppStore.class));
        }else throw new IOException("Description request message did not return a response with payload.");

    }

    private DescriptionResponseMAP expectDescriptionResponseMAP( final MessageAndPayload<?, ?> response )
            throws IOException {

        if( response instanceof DescriptionResponseMAP ) {
            return (DescriptionResponseMAP) response;

        }
        if( response instanceof RejectionMAP ) {
            final var rejectionMessage = (RejectionMessage) response.getMessage();
            throw new IOException(
                    "Message rejected by target with following Reason: " + rejectionMessage.getRejectionReason());
        }
        throw new IOException(String.format("Unexpected Message of type %s was returned",
                                            response.getMessage().getClass().toString()));
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

        final GenericMessageAndPayload messageAndPayload = new GenericMessageAndPayload(header);

        final var map = expectDescriptionResponseMAP(messageService.sendIdsMessage(messageAndPayload, appStoreURI));
        if( map.getPayload().isPresent() ) {

            return new ResourceMAP(map.getMessage(),
                                   new Serializer().deserialize(map.getPayload().get(), AppResource.class));
        }else throw new IOException("Description request message did not return a response with payload.");

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
        final GenericMessageAndPayload messageAndPayload = new GenericMessageAndPayload(header);

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
