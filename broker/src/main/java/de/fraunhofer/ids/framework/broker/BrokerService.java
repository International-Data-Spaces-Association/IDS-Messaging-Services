package de.fraunhofer.ids.framework.broker;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import de.fraunhofer.iais.eis.*;
import de.fraunhofer.ids.framework.config.ConfigContainer;
import de.fraunhofer.ids.framework.daps.*;
import de.fraunhofer.ids.framework.messaging.protocol.MessageService;
import de.fraunhofer.ids.framework.messaging.protocol.multipart.MessageAndPayload;
import de.fraunhofer.ids.framework.messaging.protocol.multipart.mapping.GenericMessageAndPayload;
import de.fraunhofer.ids.framework.messaging.protocol.multipart.mapping.MessageProcessedNotificationMAP;
import de.fraunhofer.ids.framework.messaging.protocol.multipart.mapping.RejectionMAP;
import de.fraunhofer.ids.framework.messaging.protocol.multipart.mapping.ResultMAP;
import de.fraunhofer.ids.framework.util.MultipartParseException;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

/**
 * Broker Communication Controller. Generates appropriate ids multipart messages and sends them to the broker
 * infrastructure api.
 **/
@Slf4j
@Service
public class BrokerService implements IDSBrokerService {
    private static final String                  INFO_MODEL_VERSION        = "4.0.0";


    private final ConfigContainer   container;
    private final DapsTokenProvider tokenProvider;
    private final MessageService    messageService;

    /**
     * Creates the IDSBrokerCommunication controller.
     *
     * @param container     Configuration container
     * @param tokenProvider providing DAT Token for RequestMessage
     * @param messageService providing Messaging functionality
     */
    public BrokerService( final ConfigContainer container,
                          final DapsTokenProvider tokenProvider,
                          final MessageService messageService) {
        this.container = container;
        this.tokenProvider = tokenProvider;
        this.messageService = messageService;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MessageProcessedNotificationMAP removeResourceFromBroker( final URI brokerURI, final Resource resource )
            throws IOException, DapsTokenManagerException, MultipartParseException, ClaimsException {
        logBuildingHeader();

        var securityToken = getDat();
        var connectorID = getConnectorId();

        var header = MessageBuilder
                .buildResourceUnavailableMessage(securityToken, INFO_MODEL_VERSION, connectorID, resource);

        GenericMessageAndPayload messageAndPayload = new GenericMessageAndPayload(header, resource);

        var response =  messageService.sendIdsMessage(messageAndPayload, brokerURI);
        return expectMessageProcessedNotificationMAP(response);

    }

    private MessageProcessedNotificationMAP expectMessageProcessedNotificationMAP( MessageAndPayload<?,?> response )
            throws IOException {
        RejectionMessage rejectionMessage;
        if( response instanceof MessageProcessedNotificationMAP ) {
            return (MessageProcessedNotificationMAP) response;

        }
        if (response instanceof RejectionMAP ){
            rejectionMessage = (RejectionMessage) response.getMessage();
            throw new IOException("Message rejected by target with following Reason: "+ rejectionMessage.getRejectionReason());
        }
        throw new IOException(String.format("Unexpected Message of type %s was returned", response.getMessage().getClass().toString()));
    }

    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public MessageProcessedNotificationMAP updateResourceAtBroker( final URI brokerURI, final Resource resource ) throws
            IOException,
            DapsTokenManagerException, MultipartParseException, ClaimsException {
        logBuildingHeader();

        var securityToken = getDat();
        var connectorID = getConnectorId();

        var header = MessageBuilder
                .buildResourceUpdateMessage(securityToken, INFO_MODEL_VERSION, connectorID, resource);
        GenericMessageAndPayload messageAndPayload = new GenericMessageAndPayload(header, resource);

        var response =  messageService.sendIdsMessage(messageAndPayload, brokerURI);
        return expectMessageProcessedNotificationMAP(response);
    }

    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public MessageProcessedNotificationMAP unregisterAtBroker( final URI brokerURI )
            throws IOException, DapsTokenManagerException, MultipartParseException, ClaimsException {
        logBuildingHeader();

        var securityToken = getDat();
        var connectorID = getConnectorId();

        var header = MessageBuilder.buildUnavailableMessage(securityToken, INFO_MODEL_VERSION, connectorID);
        var payload = container.getConnector();

        GenericMessageAndPayload messageAndPayload = new GenericMessageAndPayload(header, payload);

        var response =  messageService.sendIdsMessage(messageAndPayload, brokerURI);
        return expectMessageProcessedNotificationMAP(response);
    }

    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public MessageProcessedNotificationMAP updateSelfDescriptionAtBroker( final URI brokerURI ) throws
            IOException, DapsTokenManagerException, MultipartParseException, ClaimsException {
        logBuildingHeader();

        var securityToken = getDat();
        var connectorID = getConnectorId();

        var header = MessageBuilder.buildUpdateMessage(securityToken, INFO_MODEL_VERSION, connectorID);
        var payload = container.getConnector();

        GenericMessageAndPayload messageAndPayload = new GenericMessageAndPayload(header, payload);

        var response =  messageService.sendIdsMessage(messageAndPayload, brokerURI);
        return expectMessageProcessedNotificationMAP(response);

    }

    /**
     * {@inheritDoc}
     * @param brokerURIs
     */
    @Override
    public List<MessageProcessedNotificationMAP> updateSelfDescriptionAtBrokers( List<URI> brokerURIs ){
        ArrayList<MessageProcessedNotificationMAP> responses = new ArrayList<>();
        for( var uri : brokerURIs ) {
            try {
                MessageProcessedNotificationMAP response = updateSelfDescriptionAtBroker(uri);
                log.info(String.format("Received response from %s", uri));
                responses.add(response);

            } catch( IOException | MultipartParseException | ClaimsException | DapsTokenManagerException e ) {
                log.warn(String.format("Connection to Broker %s failed!", uri));
                log.warn(e.getMessage(), e);
            }
        }
        return responses;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResultMAP queryBroker( final URI brokerURI,
                                  final String query,
                                  final QueryLanguage queryLanguage,
                                  final QueryScope queryScope,
                                  final QueryTarget queryTarget )
            throws IOException, DapsTokenManagerException, MultipartParseException, ClaimsException {
        logBuildingHeader();

        var securityToken = getDat();
        var connectorID = getConnectorId();



        var header = MessageBuilder
                .buildQueryMessage(securityToken, INFO_MODEL_VERSION, connectorID, queryLanguage, queryScope,
                                   queryTarget);

        GenericMessageAndPayload messageAndPayload = new GenericMessageAndPayload(header);

        var response =  messageService.sendIdsMessage(messageAndPayload, brokerURI);
        return expectResultMAP(response);
    }

    private ResultMAP expectResultMAP( MessageAndPayload<?,?> response ) throws IOException {
            RejectionMessage rejectionMessage;
            if( response instanceof ResultMAP ) {
                return (ResultMAP) response;

            }
            if (response instanceof RejectionMAP ){
                rejectionMessage = (RejectionMessage) response.getMessage();
                throw new IOException("Message rejected by target with following Reason: "+ rejectionMessage.getRejectionReason());
            }
            throw new IOException(String.format("Unexpected Message of type %s was returned", response.getMessage().getClass().toString()));
        }


    /**
     * Get the ID of the connector
     *
     * @return Connector-ID URI
     */
    @NotNull
    private URI getConnectorId() {
        return container.getConnector().getId();
    }
    /*
     * Get a new DAT from the DAPS
     *
     * @return DAT, returned by the DAPS for the Connector
     *
     * @throws ConnectorMissingCertExtensionException Something went wrong with the Certificate of the Connector
     * @throws DapsConnectionException                The DAPS is not reachable (wrong URL, network problems..)
     * @throws DapsEmptyResponseException             The DAPS didn't return the expected response (maybe DAPS internal Problem?)
     */
    private DynamicAttributeToken getDat()
            throws ConnectorMissingCertExtensionException, DapsConnectionException, DapsEmptyResponseException {
        return tokenProvider.getDAT();
    }

    /**
     * Log info about starting to build the header
     */
    private void logBuildingHeader() {
        log.debug("Building message header");
    }

}
